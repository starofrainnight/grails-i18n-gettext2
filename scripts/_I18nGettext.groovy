includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsPackage")
includeTargets << gant.targets.Clean
includeTool << gant.tools.Execute

def getConfigValue = { what->
	def result = null
	
	try {
	   switch( what ){
			case"inputFileCharset":
				result = config.I18nGettext?.inputFileCharset?config.I18nGettext.inputFileCharset.toString():"UTF-8"
				return result 
			break
	
			case"excludedDirsArray":
				result = config.I18nGettext?.excludedDirsArray?config.I18nGettext.excludedDirsArray:[]
				return result 
			break
	
			case"noWrapPoLines":
				result = config.I18nGettext?.noWrapPoLines?true:false
				return result 
			break
			
			default:
				return null
		}
			
	} catch (Exception e) { 
		// ignore 
	}
	
	return null;
}



target( scan:"Generate .pot file from sources" ){
	depends(compile, createConfig)
	
    println("\nGenerating .pot file from sources.")

    def charset = getConfigValue( "inputFileCharset" )
    def excludedDirsArray = getConfigValue( "excludedDirsArray" )
    def noWrap = getConfigValue( "noWrapPoLines" )?"--no-wrap":""

    // trash the last .pot file
    def keysFileName = i18nDir+"/keys.pot"
    new File( keysFileName ).write("")
    
    new File(".").eachFileRecurse{ file ->
    	def currentFileCanonicalPath = file.getCanonicalPath()
    	
    	def skipThis = false
    	excludedDirsArray.any { 
        	def excludePath = new File(it).getCanonicalPath()
        	if( currentFileCanonicalPath.startsWith( excludePath ) ){
    			skipThis = true
    		}
    	} 
    	
    	if( !skipThis ){
            if( file.isFile() ){
                // switch programming language identifier for best recognition rates
                def programmingLanguageIdentifier = ""
                if( file.name.endsWith(".java") ){
                    programmingLanguageIdentifier = "java"
                } else if( file.name.endsWith(".groovy") ) {
                    programmingLanguageIdentifier = "python"		// may give better results, e.g. also include strings in single quotes.
                } else if( file.name.endsWith(".gsp") || file.name.endsWith(".jsp") ) {
                    // pretend to scan a .php file, which results in a much higher recognition rate.
                    programmingLanguageIdentifier = "php"
                } 
                    	
                if( programmingLanguageIdentifier.length()>0 ){
                    def command = 'xgettext -j --force-po '+noWrap+' -ktrc -ktr -kmarktr -ktrn:1,2 --from-code='+charset+' -o '+i18nDir+'/keys.pot -L'+programmingLanguageIdentifier+' '+file.getCanonicalPath()
                    
                    println( command )
                    def e = command.execute()
                    e.waitFor()
                    if( e.exitValue() ){
                        println( "Error: "+e.err.text )
                    }
                }
            }
    	}
    }

    mergepo()
}



target( mergepo:"Merging .po files with .pot file" ){
	depends(compile, createConfig)
	
	println( "\nMerging .po files with .pot file." )
	fileNameToCreate = "Messages"
    touchpo()        // the default Resource

    List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name
    def noWrap = getConfigValue( "noWrapPoLines" )?"--no-wrap":""

    fl.each(){
        if( !it.contains('~') ){
            String lang = it.replace( ".po", "" )

            command = 'msgmerge '+noWrap+' --backup=off -U '+i18nDir+'/'+lang+'.po '+i18nDir+'/keys.pot'
            println( command )
            def e = command.execute()
            e.waitFor()
            if( e.exitValue() ){
                println( "Error: "+e.err.text )
            }
        }
    }
}


target( makemo:"Compile .mo files" ){ params->
	depends(compile, createConfig)
	
	bundleName = "i18ngettext.Messages"
		
	if( bundle ){
	    println("\nCompiling .mo files for bundle: ${bundle}")
	    i18nOutputDir +=  "/${bundle}"
	    bundleName = "i18ngettext.${bundle}.Messages"     
	} else {
	    println("\nCompiling .mo files")
	}
    
    def destination = new File( i18nOutputDir );
    if( !destination.exists() ){
        destination.mkdir()
    }
    
    def i18nOutputDirCanonical = destination.getCanonicalPath()

    List fl = new File(i18nDir).listFiles([accept:{file->file ==~ /.*?\.po/ }] as FileFilter).toList().name
    fl.each(){
        if( !it.contains('~') ){
            String lang = it.replace( ".po", "" )

            if( lang=="Messages" ){
                command = 'msgfmt --java2 -d '+i18nOutputDirCanonical+' -r '+bundleName+' '+i18nDir+'/Messages.po' // the default Resource
            } else {
                command = 'msgfmt --java2 -d '+i18nOutputDirCanonical+' -r '+bundleName+' -l '+lang+' '+i18nDir+'/'+lang+'.po'
            }

            println( command )
            def e = command.execute()
            e.waitFor()
            if( e.exitValue() ){
                println( "Error: "+e.err.text )
            }
        }
    }
    
    def jarName = bundle ? "i18ngettext-${bundle}.jar" : "i18ngettext.jar"
    ant.jar( basedir:"${i18nOutputDirCanonical}", includes:"i18ngettext/**/*", destfile:"./lib/${jarName}")    
}


target( touchpo:"Initialize first .po file" ) { params->
	depends(compile, createConfig)
	
	def charset = getConfigValue( "inputFileCharset" )
    def header = """
# SOME DESCRIPTIVE TITLE.
# Copyright (C) YEAR AUTHOR
# This file is distributed under the same license as the PACKAGE package.
# FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.
#
#, fuzzy
msgid ""
msgstr ""
"Project-Id-Version: PACKAGE VERSION\\n"
"Report-Msgid-Bugs-To: \\n"
"POT-Creation-Date: YEAR-MO-DA HO:MO+ZONE\\n"
"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n"
"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n"
"Language-Team: LANGUAGE <LL@li.org>\\n"
"MIME-Version: 1.0\\n"
"Content-Type: text/plain; charset=${charset}\\n"
"Content-Transfer-Encoding: 8bit\\n"
"""

    if( fileNameToCreate.length()>0 ){
		
		def fileName = fileNameToCreate.replace( ".po", "" )
        def destination = new File( ""+i18nDir+'/'+fileName+'.po' );

        if( destination.exists() ){
        	if( fileName != "Messages" ){
            	println( "File: "+destination.getCanonicalPath()+" already exists. Will not recreate it.")
        	}
        } else {
            if( fileName=="Messages" ){
            	// write our default header to the file
            	destination.write( header, 'UTF-8' )
            } else {
                // make sure the "Messages.po" file exists
            	fileNameToCreate = "Messages"
                touchpo()

                def source = new File( ""+i18nDir+'/Messages.po' );
                if( source ){
                    // copy the "Messages.po" file to the new name.
                    destination.withOutputStream{ out->out.write source.readBytes() }
                } else {
                    // write our default header to the file
                    destination.write( header, 'UTF-8' )
                }
            	println( "File: "+destination.getCanonicalPath()+" has been created.")
            }
        }
    }
}
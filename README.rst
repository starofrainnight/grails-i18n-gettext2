grails-i18n-gettext2
====================

Purpose
------------------
This project is cloned from https://svn.codehaus.org/grails-plugins/grails-i18n-gettext/ 

There no new updates after 2010-11-02, it's not works for grails 2.x later.

So I fork from 0.98 and renamed project to grails-i18n-gettext2 ( Means grails-i18n-gettext for 2.x )

Description
------------------
**i18n-gettext plugin**

This plugin adds i18n support to your app, in 'gnu gettext'-style. (see: gettext homepage)

 .. line-block::
   No more variables (".properties" keys) instead of translatable strings in your code    
   No more obsolete translations you will not be able to identify after some time    
   No more telling translators about how to handle ".properties" files without destroying them    
   Very easy plural handling. You will love it.    
   MessageFormat-like string concatenation    
   More readable code, because you know exactly what text will be visible to users    
   Does not interfere with your existing translations, so you may keep your 'legacy' translations and start transition to i18n-gettext today    
   Extracting strings and merging translations can be automated by scripts

This plugin adds i18n support to your app, in 'gnu gettext'-style.

1) First, you need to wrap special tags or service calls around all strings you want to translate.

2) Then you call "grails i18n-gettext" to extract all translatable strings from your sources.

3) Now you translate all strings from step 2 which you will find in .po files in your i18n directory.

4) Call "grails i18n-gettext makemo" to compile your translated .po files into resource classes.

5) repeat 1-4 each time you added some new strings to your application. Existing translations will be merged in.

During runtime: The methods, you wrapped around the strings, will pick the correct translation according to the current locale, and return the translated string. You may also force a locale for a specific call.

What you need: The developer needs these command line tools for the development machine: xgettext, msgmerge and msgfmt The translator may like: PoEdit or alike to translate texts.

You will love the dead simple plural form handling and FormatMessage-like String handling. Additionally, you can forget about inventing lookup keys for your .properties files, because for gnu gettext, the original string is the key. Plus: No more problems with special chars like german umlauts. gettext can handle that.

Beware: Gnu gettext can not handle groovy's "here-doc" strings.

Installation
------------

To install the plugin, type this command in your project's root folder:

 .. code:: bash
 
	grails install-plugin i18n-gettext

Additional requirements
```````````````````````

You need the tools: xgettext, msgmerge and msgfmt for your development machine. The translator may like: PoEdit or alike to translate texts.

Optional configuration
``````````````````````

You may set variables that cannot be guessed by convention in "Config.groovy".

* In case your whole translation chain is using a different charset than UTF-8, you can set that in "inputFileCharset". You ought to know what you're doing, because mixing charsets can bring you a lot of hair-pulling.

* If the original texts in your code are not english, you should set the "sourceCodeLocale" to the locale of your texts.

 .. code:: java
	
	I18nGettext {                                                                                                              
		inputFileCharset = "UTF-8"                                                                                          
		sourceCodeLocale = "en"                                                                                                 		
	}

Tip: Use UTF-8 for your source code files, po files and content-type of your delivered pages. Tell your favourite code editor about that, too. In case you receive warnings about charset >>en<< when executing

 .. line-block::
	grails i18n-gettext

, check all .po files in i18n directory. There you'd better have this line amongst the header lines:

 .. line-block::
	"Content-Type: text/plain; charset=UTF-8n"

Usage
-----

Preparing your code
```````````````````
To make translation possible, there are tags or service methods you need to wrap around all texts you'd like to translate. This has two effects:

1) All text inside the wrapping methods will later be collected by the xgettext tool for translation.

2) The wrapping methods later actually will return the translated string during runtime. Any string that has not been translated to another language will be returned in the original language from your source code.

Note: Empty strings "" may be 'translated' to a default text from the header of your translation files. Do not pass empty strings to the wrapper methods. Note: When using the taglib of this plugin, the order of attributes is important and will be enforced by the service and the taglib. xgettext relies on the correct order of the attributes.

Domain classes and controller classes
`````````````````````````````````````
In domain classes, use the t9nService by defining the service and declaring the service as transient. You use the translation methods via the service.

 .. line-block::
	def t9nService
	static transients = ['t9nService']

	def translated string = t9nService.tr( s:"String to translate" )

In controller classes, you can access all translation methods via the taglib namespace:

 .. line-block::
	t9n.tr( s"string to translate" )

The methods/tags work like this:

 .. line-block::
	// returns: the current locale as a string, e.g. "de_DE"
	getCurrentLocale()


	// returns: the translation of "foo" according to the current session's locale
	tr( s:"foo" )
	// returns: the german translation of "foo"
	tr( s:"foo", locale:"de" )


	// returns: the translation of "foo" according to the current session's locale, 
	// and concatenated with the untranslated word "bar"
	tr( s:"foo{0}", f:["bar"] )

	// returns: the german translation of "foo", 
	// concatenated with the untranslated word "bar"
	tr( s:"foo{0}", f:["bar"], locale:"de" )

	// same as above, only with more concatenated strings
	tr( s:"foo{0} and foo{1}", f:["bar", "baz"] )		
	tr( s:"foo{0} and foo{1}", f:["bar", "baz"], locale:"de" )

	// returns: the translation of "foo" in singular form, 
	// according to the current session's locale
	trn( s:"foo", p:"foos", n:1 )						
	// returns: the german translation of "foo" (in singular form)
	trn( s:"foo", p:"foos", n:1, locale:"de" )

	// same, but with MessageFormat-like string concatenation
	trn( s:"foo{0}", p:"foos{0}", n:1, f:["bar"] )						
	trn( s:"foo{0}", p:"foos{0}", n:1, f:["bar"], locale:"de" )


	// returns: the translation of "foos" in plural form, 
	// according to the current session's locale
	trn( s:"foo", p:"foos", n:2 )						
	// returns: the german translation of "foos" (in plural form)
	trn( s:"foo", p:"foos", n:2, locale:"de" )

	// same, but with MessageFormat-like string concatenation
	trn( s:"foo{0}", p:"foos{0}", n:2, f:["bar"] )						
	trn( s:"foo{0}", p:"foos{0}", n:2, f:["bar"], locale:"de" )


	// Are you still with me ? Presenting the most complicated example:
	// The following will result in: "schnicksbazs and schnicksbars" 
	// if your german ("de") translation of "foos{3} and foos{2}" 
	// is "schnicks{3} and schnicks{2}"
	// and your source code locale is not "de".
	trn( s:"foo{2} and foo{3}", p:"foos{3} and foos{2}", n:2, f:["bar", "baz", "bars", "bazs"], locale:"de" )

	// Disambiguates texts. returns: "foo", 
	// if current locale is the same as the source code locale.
	// If locales are different, returns translation of "foo (verb)" 
	// or "foo (noun)". "(verb)" and "(noun)" are tranlsation hints 
	// for your translators.
	// trc() is the main reason for the existence of the sourceCodeLocale setting 
	// in Config.groovy
	trc( c:"foo (verb)", s:"foo" )
	trc( c:"foo (noun)", s:"foo" )

	// same as above, but with a forced locale of "de"
	trc( c:"foo (verb)", s:"foo", locale:"de" )
	trc( c:"foo (noun)", s:"foo", locale:"de" )

	// mark for tranlsation, but always return the original text.
	marktr( s:"foo" )

View classes
````````````
The way the translation methods work is the same as with controllers, via the t9n namespace. In the .gsp files of your views, you call the tags like methods:

 .. line-block::
	// several examples of method calls:
	<%=t9n.getCurrentLocale() %>
	<%=t9n.tr( s:"foo{0}", f["bar"] ) %>
	<%=t9n.trn( s:"foo", p:"foos", n:42 ) %>
	<%=t9n.trc( c:"foo (verb)", s:"foo" ) %>
	<%=t9n.marktr( s:"foo" ) %>

Running included scripts
````````````````````````
i18n-gettext comes with some scripts that help you collect all translatable strings, and in the end compile all translations into ResourceBundle classes. These classes in turn are used by i18n-gettext to translate your texts at runtime.

First of all, you add new locales to your project which you want to translate later. You can add new locales at any time, so you're safe if you decide to translate your app into any other language after 2 years in production. For each locale you add, you will find a ".po" file in your project's i18n directory. It will be filled with translatable strings by the next script. A "Messages.po" file will also be generated. It's a kind of fallback file, and you should not translate its contents. If you wish, think of it as the "null"-locale. Existing ".properties" files in your i18n directory won't be touched at all.

 .. line-block::
	// Add a new locale to your code. 
	// The locale's name follows the usual conventions 
	// ("de_DE", "de", "en_US", "en", yaddayadda...)
	grails i18n-gettext init de

Note: xgettext cannot handle groovy's here-doc strings.

The following call will collect all translatable strings that have been wrapped by a tr(), trn(), trc(), or marktr() call from your project's ".groovy", ".gsp", ".java" and ".jsp" files. The strings will be added to all ".po" files in your i18n directory. Existing .properties files won't be touched. You hand out the ".po" files to your translators. When you get them back, you put them back into your i18n directory. Each time you run the collection again, all changes will be merged into your ".po" files automagically.

 .. line-block::
	grails i18n-gettext

Tip: If a translation is missing in any of your ".po" files, the original string from your source code will be shown.

After your translators did their work, you use the following script to compile all translations into ResourceBundle class files. When done, these files will live in the "web-app/WEB-INF/i18n-gettext/" directory - ready for production.

 .. line-block::
	grails i18n-gettext makemo

To create a message bundle with a different name, call:

 .. line-block::
	grails i18n-gettext makemo anybundlename

To fetch strings from that specific bundle, state the bundle name in your t9n calls, like:

 .. line-block::
	t9n.tr( s:"foo", bundle:"anybundlename" )

Testing
```````
The plugin itself has a built-in integration test. Before you can run it, you must call:

 .. line-block::
	grails i18n-gettext makemo

to generate the ResourceBundle class files for the test.

License
-------
This plugin is published under the Apache License, Version 2.0

Thanks
------
This plugin is using the Java internationalization library Gettext Commons (under LGPL license) Thanks to all contributors, thanks for the valuable feedback from users, and many thanks to the great grails dev team!

Plugin version history
----------------------

0.98 (2010-11-01)

* HUGE performance boost, caching fix, thread-safety fix and added bundle support. Updating to this release is strongly recommended if you love your own application, i18n and performance

0.94 (2010-05-30)

* bug fix release

0.93 (2010-05-21)

* small but important fix that makes it possible to have special chars in your original strings. Kudos to Ales from the grails user mailing list !

0.92 (2010-05-20)

* upgraded to grails 1.3.1
* fixed gant scripts

0.84 (2009-03-11)

* makemo ant task fixed
* upgraded to grails 1.1

0.83 (2009-03-02)

* Cleaned up path handling and generating a i18n-gettext.jar file in /lib as a result of makemo
* Built with 1.1-RC2

0.8 (2009-02-24)

* Use translation methods via taglib namespace t9n in views, controllers and taglibs
* Use translation methods via t9nService in domains and service classes.
* Use the same attributes for both, and RESPECT THE ORDER OF ATTRIBUTES. (xgettext requirement)

0.1 (2008-10-14)

* Initial release

//
//   Copyright 2008 Rainer Brang
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

import java.text.MessageFormat
import java.util.Locale
import org.xnap.commons.i18n.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.web.context.request.RequestContextHolder as RCH
import java.lang.IllegalArgumentException

class T9nService {

    boolean transactional = false
    
    
    /**
     * @param s - the text to translate
     * @param f - the messageFormat of single
     * @param locale - the locale to translate into
     * @param sourceLocale - the locale of the source
     * @return the translation of single
     */
	def tr = { attrs ->
	
		if( !attrs || !attrs.s ){
		}
		if( attrs?.c ){
			log.info "Tag [tr] does not accept attribute [c]"
		}
		if( attrs?.p ){
			log.info "Tag [tr] does not accept attribute [p]"
		}
		if( attrs?.n ){
			log.info "Tag [tr] does not accept attribute [n]"
		}
		
		// check attribute order
		def theKeys = attrs.keySet().toArray();
		if( !"s".equals(theKeys[0]) ){
			throwError "Tag [tr] requires attribute [s] to be the first attribute"
		}
	
		def i18n = getI18nObject( attrs.locale, attrs.sourceLocale )
		return i18n ? (i18n.tr( attrs.s, (Object[])attrs.f)) : (MessageFormat.format(attrs.s, (Object[])attrs.f))
	}     


	/**
		if( !attrs || !attrs.s || !attrs.p || !attrs.n ){
		return i18n ? (i18n.trn( attrs.s, attrs.p, attrs.n, (Object[])attrs.f )) : (attrs.n>1? (MessageFormat.format(attrs.p, (Object[])attrs.f)) : (MessageFormat.format(attrs.s, (Object[])attrs.f)) )
	}

	
	/**
    
    
	/**
		
		
		try{
			def request = RCH.currentRequestAttributes().currentRequest
			def session = RCH.currentRequestAttributes().session

			currentLocale = session?.getAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE")
			currentLocale = currentLocale?currentLocale:request?.getLocale()
		} catch( Exception e ){
			// no implementation. This could be an IllegalStateException because we try to access the session during bootstrap when there is no session available... 
		}
				
		// Fallbacks		
		if( !currentLocale ){
			currentLocale = ApplicationHolder?.application?.config?.I18nGettext?.sourceCodeLocale ?:"en"			
		}
		
		return currentLocale.toString()		
	}
    
    
    /**
     * Get an I18n from the I81nFactoy and set the current locale and source code locale.
     */
	private getI18nObject( wantLocale=null, forceSourceCodeLocale=null ) {
    	
		def i18n = null
		try{
			
			def language = ""
			def country = ""
			def variant = ""
			
			if ( !wantLocale ){
				wantLocale = getCurrentLocale()
			}
			def wantedLocale = null
			try{
				language = wantLocale?.split("_")[0].toLowerCase()
				country = wantLocale?.split("_")[1].toUpperCase()
				variant = wantLocale?.split("_")[2]
			} catch( ArrayIndexOutOfBoundsException aioe0 ){
				// ignore
			}
			wantedLocale = new Locale( language, country, variant )
			
			// use source code locale string forced by the method call or from config or use the bailout "en"
			if ( !forceSourceCodeLocale ){
			}
			def wantedSourceCodeLocale = null
			language = ""
			country = ""
			variant = ""
			try{
				language = forceSourceCodeLocale?.split("_")[0].toLowerCase()
				country = forceSourceCodeLocale?.split("_")[1].toUpperCase()
				variant = forceSourceCodeLocale?.split("_")[2]
			} catch( ArrayIndexOutOfBoundsException aioe1 ){
				// ignore
			}
			wantedSourceCodeLocale = new Locale( language, country, variant )
			
			i18n = I18nFactory.getI18n( T9nService.class, "i18ngettext.Messages" )
			i18n.setSourceCodeLocale( wantedSourceCodeLocale )
			i18n.setLocale( wantedLocale )
			
		} catch( MissingResourceException mre ){
			log.error( mre.getMessage()+". Key: "+mre.getKey()+" Class: "+mre.getClassName() )
			return null
		}
		return i18n
	}

	
}
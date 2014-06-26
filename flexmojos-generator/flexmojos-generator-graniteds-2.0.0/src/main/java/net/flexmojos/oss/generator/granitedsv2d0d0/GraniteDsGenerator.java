/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.generator.granitedsv2d0d0;

import static java.lang.Thread.currentThread;
import static org.granite.generator.template.StandardTemplateUris.BEAN;
import static org.granite.generator.template.StandardTemplateUris.BEAN_BASE;
import static org.granite.generator.template.StandardTemplateUris.ENTITY;
import static org.granite.generator.template.StandardTemplateUris.ENTITY_BASE;
import static org.granite.generator.template.StandardTemplateUris.ENUM;
import static org.granite.generator.template.StandardTemplateUris.INTERFACE;
import static org.granite.generator.template.StandardTemplateUris.TIDE_ENTITY_BASE;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.granite.generator.Generator;
import org.granite.generator.Output;
import org.granite.generator.TemplateUri;
import org.granite.generator.Transformer;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.gsp.GroovyTemplateFactory;

import net.flexmojos.oss.generator.GenerationException;
import net.flexmojos.oss.generator.GenerationRequest;

/**
 * @author edward.yakop@gmail.com
 * @since 3.2
 */
@Component( role = net.flexmojos.oss.generator.Generator.class, hint = "graniteds2" )
public final class GraniteDsGenerator
    extends AbstractLogEnabled
    implements net.flexmojos.oss.generator.Generator
{

    private static final String PREFIX_TO_REPLACE = "class:";

    private static final String SHADED_PREFIX = "class:";

    private String uid = "uid";

    private String as3typefactory = null;

    private boolean tide = false;

    private boolean generateEnumToTransientStorage = false;

    private boolean generateInterfaceToTransientStorage = false;

    private String transformer = null;

    private List<PackageTranslator> translators = new ArrayList<PackageTranslator>();

    // /////////////////////////////////////////////////////////////////////////
    // Configuration implementation fields.
    private As3TypeFactory as3TypeFactoryImpl = null;

    private GroovyTemplateFactory groovyTemplateFactory = null;

    private TemplateUri[] entityTemplateUris;

    private TemplateUri[] interfaceTemplateUris;

    private TemplateUri[] beanTemplateUris;

    private TemplateUri[] enumTemplateUris;

    private Map<String, File> classes;

    private File outputDirectory;

    private File baseOutputDirectory;

    private TemplateUri[] initializeEnumTemplateURIs( String[] enumTemplate )
    {
        String templateUri = getStringIndex0( enumTemplate );
        if ( templateUri == null )
        {
            templateUri = ENUM.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        }
        return createTemplateUris( null, templateUri );
    }

    private TemplateUri[] initializeInterfaceTemplateURIs( String[] interfaceTemplate )
    {
        String baseTemplateUri = null;
        String templateUri = INTERFACE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( getStringIndex1( interfaceTemplate ) != null )
        {
            templateUri = getStringIndex1( interfaceTemplate );
        }

        if ( getStringIndex0( interfaceTemplate ) != null )
        {
            baseTemplateUri = getStringIndex0( interfaceTemplate );
        }
        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private TemplateUri[] initializeEntityTemplateURIs( String[] entityTemplate )
    {
        String baseTemplateUri = ENTITY_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        String templateUri = ENTITY.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( getStringIndex1( entityTemplate ) != null )
        {
            templateUri = getStringIndex1( entityTemplate );
        }

        if ( getStringIndex0( entityTemplate ) != null )
        {
            baseTemplateUri = getStringIndex0( entityTemplate );
        }
        else if ( tide )
        {
            baseTemplateUri = TIDE_ENTITY_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        }

        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private TemplateUri[] initializeBeanTemplateURIs( String[] beanTemplate )
    {
        String baseTemplateUri = BEAN_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        String templateUri = BEAN.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( getStringIndex1( beanTemplate ) != null )
        {
            templateUri = getStringIndex1( beanTemplate );
        }
        if ( getStringIndex0( beanTemplate ) != null )
        {
            baseTemplateUri = getStringIndex0( beanTemplate );
        }

        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private String getStringIndex0( String[] a )
    {
        return getStringByIndex( a, 0 );
    }

    private String getStringIndex1( String[] a )
    {
        return getStringByIndex( a, 1 );
    }

    private String getStringByIndex( String[] a, int index )
    {
        String s = a == null ? null : ( a.length < index + 1 ? null : a[index] );
        return s == null ? null : new File( s ).toURI().toString();
    }

    private TemplateUri[] createTemplateUris( String baseUri, String uri )
    {
        TemplateUri[] templateUris = new TemplateUri[baseUri == null ? 1 : 2];
        int i = 0;
        if ( baseUri != null )
        {
            templateUris[i++] = new TemplateUri( baseUri, true );
        }
        templateUris[i] = new TemplateUri( uri, false );
        return templateUris;
    }

    public final void generate( GenerationRequest request )
        throws GenerationException
    {
    	
    	// add / create package translators
    	for(String currentTranslator : request.getTranslators()){
    		String[] splitTranslator = currentTranslator.split("=");
    		if(splitTranslator.length != 2){
    			throw new GenerationException("Invalid format: translators must be in format 'java.package=as3.package'");
    		}
    		String java = splitTranslator[0];
    		String as3 = splitTranslator[1];
    		
    		getLogger().info("Adding translator: [" + java + ", " + as3 + "]");
    		translators.add(new PackageTranslator(java, as3));
    	}
    	// tide
    	String useTide = request.getExtraOptions().get("tide");
    	if(useTide != null)
    	{
    		tide = Boolean.valueOf(useTide.trim());
    	}

        // Enums and Interfaces don't have Base classes, per default these
        // classes are generated into the checked-in code part. As these classes
        // are overwritten, someone will probably want them to be generated
        // to the transient (target/generated-source) directory.
        String useGenerateEnumToTransientStorage = request.getExtraOptions().get( "generateEnumToTransientStorage" );
        if ( useGenerateEnumToTransientStorage != null )
        {
            generateEnumToTransientStorage = Boolean.valueOf(useGenerateEnumToTransientStorage.trim());
        }
        String useGenerateInterfaceToTransientStorage = request.getExtraOptions().get( "generateInterfaceToTransientStorage" );
        if ( useGenerateInterfaceToTransientStorage != null )
        {
            generateInterfaceToTransientStorage = Boolean.valueOf(useGenerateInterfaceToTransientStorage.trim());
        }

        uid = request.getExtraOptions().get( "uid" );
        
        transformer = request.getExtraOptions().get( "transformer" );

        outputDirectory = request.getPersistentOutputFolder();
        
        baseOutputDirectory = request.getTransientOutputFolder();
		
        String[] enumTemplate = getTemplate( request, "enum-template" );
        enumTemplateUris = initializeEnumTemplateURIs( new String[]{enumTemplate[1]} );

        String[] interfaceTemplate = getTemplate( request, "interface-template" );
        interfaceTemplateUris = initializeInterfaceTemplateURIs( interfaceTemplate );

        String[] entityTemplate = getTemplate( request, "entity-template" );
        entityTemplateUris = initializeEntityTemplateURIs( entityTemplate );

        String[] beanTemplate = getTemplate( request, "bean-template" );
        beanTemplateUris = initializeBeanTemplateURIs( beanTemplate );


        classes = request.getClasses();
        if ( classes.isEmpty() )
        {
            getLogger().warn( "No classes to generate." );
            return;
        }

        ClassLoader classLoader = request.getClassLoader();

        ClassLoader originalClassLoader = currentThread().getContextClassLoader();
        currentThread().setContextClassLoader( classLoader );

        try
        {
            // As3TypeFactory.
            as3typefactory = request.getExtraOptions().get("as3typefactory");
            if ( as3typefactory == null )
            {
                as3TypeFactoryImpl = new DefaultAs3TypeFactory();
            }
            else
            {
                getLogger().info( "Instantiating custom As3TypeFactory class: [" + as3typefactory + "]" );
                as3TypeFactoryImpl = newInstance( classLoader, as3typefactory );
            }

            // Listener.
            GeneratorConfiguration configuration = new GeneratorConfiguration();

            // Transformer
            Transformer<?, ?, ?> transformerImpl = createTransformer( classLoader, configuration );

            // Create the generator.
            Generator generator = new Generator( configuration );
            generator.add( transformerImpl );
            
            // Call the generator for each class
            getLogger().info( "Calling the generator for each Java class." );
            int count = generateClass( classLoader, generator );
            getLogger().info( "Files affected: " + count + ( count == 0 ? " (nothing to do)." : "." ) );
        }
        finally
        {
            currentThread().setContextClassLoader( originalClassLoader );
        }
    }

    private String[] getTemplate( GenerationRequest configuration, String name )
    {
        String baseTemplate = configuration.getTemplates().get( "base-" + name );
        String template = configuration.getTemplates().get( name );
        return new String[] { baseTemplate, template };
    }

    private Transformer<?, ?, ?> createTransformer( ClassLoader classLoader, GeneratorConfiguration configuration )
        throws GenerationException
    {
        Transformer<?, ?, ?> transformerImpl;
        if ( transformer != null )
        {
            getLogger().info( "Instantiating custom Transformer class: [" + transformer + "]" );
            transformerImpl = newInstance( classLoader, transformer );
        }
        else
        {
            transformerImpl = new JavaAs3GroovyTransformer();
        }
        transformerImpl.setListener( new Gas3Listener( getLogger() ) );
        return transformerImpl;
    }

    private int generateClass( ClassLoader classLoader, Generator generator )
        throws GenerationException
    {
        int count = 0;
        for ( Map.Entry<String, File> classFile : classes.entrySet() )
        {
            String className = classFile.getKey();
            try
            {
                Class<?> classToGenerate = classLoader.loadClass( className );
				if( classToGenerate.isMemberClass() && !classToGenerate.isEnum() )
				{
					continue;
				}
                JavaAs3Input input = new JavaAs3Input( classToGenerate, classFile.getValue() );
                
                for ( Output<?> output : generator.generate( input ) )
                {
                    if ( output.isOutdated() )
                    {
                        count++;
                    }
                }
            }
            catch ( Exception e )
            {
                getLogger().error( getStackTrace( e ) );
                throw new GenerationException( "Fail to generate class [" + className + "]", e );
            }
        }
        return count;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Utilities.

    @SuppressWarnings( "unchecked" )
    private <T> T newInstance( ClassLoader loader, String className )
        throws GenerationException
    {
        try
        {
            return (T) loader.loadClass( className ).newInstance();
        }
        catch ( Exception e )
        {
            throw new GenerationException( "Instantiate [" + className + "] failed." );
        }
    }

    private static String getStackTrace( Exception e )
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        return sw.toString();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Generator configuration
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class GeneratorConfiguration
        implements JavaAs3GroovyConfiguration
    {

        public String getUid()
        {
            return uid;
        }

        public boolean isGenerated( Class<?> clazz )
        {
            return (!clazz.isMemberClass() || clazz.isEnum()) && classes.containsKey(clazz.getName());
        }

        public As3TypeFactory getAs3TypeFactory()
        {
            return as3TypeFactoryImpl;
        }

        public List<PackageTranslator> getTranslators()
        {
            return translators;
        }

        public TemplateUri[] getTemplateUris( JavaType.Kind kind, Class<?> clazz )
        {
            switch ( kind )
            {
                case ENTITY:
                    return entityTemplateUris;
                case INTERFACE:
                    return interfaceTemplateUris;
                case ENUM:
                    return enumTemplateUris;
                case BEAN:
                    return beanTemplateUris;
                default:
                    throw new IllegalArgumentException( "Unknown template kind: " + kind + " / " + clazz );
            }
        }

        public File getOutputDir( JavaAs3Input input )
        {
            switch (input.getJavaType().getKind()) {
                case INTERFACE:
                    if(generateInterfaceToTransientStorage) {
                        return baseOutputDirectory;
                    } else {
                        return outputDirectory;
                    }
                case ENUM:
                    if(generateEnumToTransientStorage) {
                        return baseOutputDirectory;
                    } else {
                        return outputDirectory;
                    }
                default:
                    return outputDirectory;
            }
        }

        public File getBaseOutputDir( JavaAs3Input input )
        {
            return baseOutputDirectory;
        }

        public GroovyTemplateFactory getGroovyTemplateFactory()
        {
            if ( groovyTemplateFactory == null )
            {
                groovyTemplateFactory = new GroovyTemplateFactory();
            }
            return groovyTemplateFactory;
        }

        public ClassLoader getClassLoader()
        {
            return currentThread().getContextClassLoader();
        }
    }
}
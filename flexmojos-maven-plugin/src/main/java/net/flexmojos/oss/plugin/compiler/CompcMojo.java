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
package net.flexmojos.oss.plugin.compiler;

import static net.flexmojos.oss.plugin.common.FlexExtension.RB_SWC;
import static net.flexmojos.oss.plugin.common.FlexExtension.SWC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import net.flexmojos.oss.compiler.ICompcConfiguration;
import net.flexmojos.oss.compiler.IIncludeFile;
import net.flexmojos.oss.compiler.IIncludeStylesheet;
import net.flexmojos.oss.compiler.command.Result;
import net.flexmojos.oss.plugin.compiler.attributes.MavenIncludeStylesheet;
import net.flexmojos.oss.plugin.compiler.attributes.SimplifiablePattern;
import net.flexmojos.oss.util.PathUtil;

/**
 * <p>
 * Goal which compiles the Flex sources into a library for either Flex or AIR depending.
 * </p>
 * <p>
 * The Flex Compiler plugin compiles all ActionScript sources. It can compile the source into 'swc' files. The plugin
 * supports the 'swc' packaging.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal compile-swc
 * @requiresDependencyResolution compile
 * @phase compile
 * @threadSafe
 */
public class CompcMojo
    extends AbstractFlexCompilerMojo<ICompcConfiguration, CompcMojo>
    implements ICompcConfiguration, Mojo
{

    /**
     * Writes a digest to the catalog.xml of a library. This is required when the library will be used as runtime shared
     * libraries
     * <p>
     * Equivalent to -compute-digest
     * </p>
     * 
     * @parameter expression="${flex.computeDigest}"
     */
    protected Boolean computeDigest;

    /**
     * Output the library as an open directory instead of a SWC file
     * <p>
     * Equivalent to -directory
     * </p>
     * 
     * @parameter expression="${flex.directory}"
     */
    private Boolean directory;

    /**
     * Automatically include all declared namespaces
     * 
     * @parameter default-value="false" expression="${flex.includeAllNamespaces}"
     */
    private boolean includeAllNamespaces;

    /**
     * Inclusion/exclusion patterns used to filter classes to include in the output SWC.
     * ** denotes a directory wildcard, * denotes a file wildcard.  For example: 
     * 
     * <p>
     * **.model.* -- denotes all classes in any package named "model"
     * </p>
     *
     * <p>
     * Equivalent to -include-classes
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeClasses&gt;
     *   &lt;include&gt;net.flexmojos.oss.MyClass&lt;/include&gt;
     *   &lt;include&gt;net.flexmojos.oss.YourClass&lt;/include&gt;
     *   &lt;scan&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;**.*&lt;/include&gt;
     *     &lt;/includes&gt;
     *   &lt;/scan&gt;
     *   &lt;scan&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;com.mycompany.*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;com.mycompany.ui.*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/scan&gt;     
     *   &lt;scan&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;org.mycompany.*&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;org.mycompany.ui.*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/scan&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    private SimplifiablePattern includeClasses;

    /**
     * Inclusion/exclusion patterns used to filter resources to be include in the output SWC
     * <p>
     * Equivalent to -include-file
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeFiles&gt;
     *   &lt;include&gt;afile.xml&lt;/include&gt;
     *   &lt;include&gt;b.txt&lt;/include&gt;
     *   &lt;scan&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;**&#47;*.mxml&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;private/*&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/scan&gt;
     * &lt;/includeFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    protected SimplifiablePattern includeFiles;

    /**
     * If true, manifest entries with lookupOnly=true are included in SWC catalog
     * <p>
     * Equivalent to -include-lookup-only
     * </p>
     * 
     * @parameter expression="${flex.includeLookupOnly}"
     */
    private Boolean includeLookupOnly;

    /**
     * All classes in the listed namespaces are included in the output SWC
     * <p>
     * Equivalent to -include-namespaces
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeNamespaces&gt;
     *   &lt;namespace&gt;http://mynamespace.com&lt;/namespace&gt;
     * &lt;/includeNamespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeNamespaces;

    /**
     * A list of directories and source files to include in the output SWC
     * <p>
     * Equivalent to -include-sources
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;includeSource&gt;${project.build.sourceDirectory}&lt;/includeSource&gt;
     * &lt;/includeSources&gt;
     * </pre>
     * 
     * @parameter
     */
    private File[] includeSources;

    /**
     * A list of named stylesheet resources to include in the output SWC
     * <p>
     * Equivalent to -include-stylesheet
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeStylesheets&gt;
     *   &lt;stylesheet&gt;
     *     &lt;name&gt;mystyle.css&lt;/name&gt;
     *     &lt;path&gt;${basedir}/mystyle.css&lt;/path&gt;
     *   &lt;/stylesheet&gt;
     * &lt;/includeStylesheets&gt;
     * </pre>
     * 
     * @parameter
     */
    private MavenIncludeStylesheet[] includeStylesheets;

    /**
     * DOCME Guess what, undocumented by adobe. Looks like it was overwritten by source paths
     * <p>
     * Equivalent to -root
     * </p>
     * 
     * @parameter expression="${flex.root}"
     * @deprecated
     */
    private String root;

    @Override
    public Result doCompile( ICompcConfiguration cfg, boolean synchronize )
        throws Exception
    {
        return compiler.compileSwc( cfg, synchronize, compilerName );
    }

    public void fmExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !( PathUtil.existAny( getSourcePath() ) || getIncludeFile() != null ) )
        {
            getLog().info( "Skipping compiler, nothing available to be included on swc." );
            return;
        }

        executeCompiler( this, true );

        if ( getLocalesRuntime() != null )
        {
            List<Result> results = new ArrayList<Result>();
            for ( String locale : getLocalesRuntime() )
            {
                CompcMojo cfg = this.clone();
                configureResourceBundle( locale, cfg );
                cfg.getCache().put( PROJECT_TYPE, RB_SWC );
                results.add( executeCompiler( cfg, fullSynchronization ) );
            }

            wait( results );
        }
    }

    public Boolean getComputeDigest()
    {
        return computeDigest;
    }

    public Boolean getDirectory()
    {
        return directory;
    }

    public List<String> getIncludeClasses()
    {
        if ( includeClasses == null )
        {
            return null;
        }

        List<String> classes = new ArrayList<String>();

        classes.addAll( includeClasses.getIncludes() );
        classes.addAll( filterClasses( includeClasses.getPatterns(), getSourcePath() ) );

        return classes;
    }

    public IIncludeFile[] getIncludeFile()
    {
        List<IIncludeFile> files = new ArrayList<IIncludeFile>();

        List<FileSet> patterns = new ArrayList<FileSet>();
        if ( includeFiles == null && includeNamespaces == null && includeSources == null && includeClasses == null )
        {
            patterns.addAll( resources );
        }
        else if ( includeFiles == null )
        {
            return null;
        }
        else
        {
            // process patterns
            patterns.addAll( includeFiles.getPatterns() );

            // process files
            for ( final String path : includeFiles.getIncludes() )
            {
                final File file = PathUtil.file( path, getResourcesTargetDirectories() );

                if ( file == null )
                {
                    throw new IllegalStateException(
                                                     "Unable to resolve include file, path: '"
                                                         + path
                                                         + "'. Please ensure that the file exists. Note: relative paths must be relative to a resource target directory." );
                }

                files.add( new IIncludeFile()
                {
                    public String name()
                    {
                        return path.replace( '\\', '/' );
                    }

                    public String path()
                    {
                        return file.getAbsolutePath();
                    }
                } );
            }
        }

        for ( FileSet pattern : patterns )
        {
            final DirectoryScanner scan = scan( pattern );
            if ( scan == null )
            {
                continue;
            }

            for ( final String file : scan.getIncludedFiles() )
            {
                files.add( new IIncludeFile()
                {
                    public String name()
                    {
                        return file.replace( '\\', '/' );
                    }

                    public String path()
                    {
                        return PathUtil.file( file, scan.getBasedir() ).getAbsolutePath();
                    }
                } );
            }
        }

        return files.toArray( new IIncludeFile[0] );
    }

    public Boolean getIncludeLookupOnly()
    {
        return includeLookupOnly;
    }

    public List<String> getIncludeNamespaces()
    {
        if ( includeNamespaces != null )
        {
            return includeNamespaces;
        }

        if ( includeAllNamespaces )
        {
            return getNamespacesUri();
        }

        return null;
    }

    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    public File[] getIncludeSources()
    {
        if ( includeFiles == null && getIncludeNamespaces() == null && includeSources == null && includeClasses == null )
        {
            return getSourcePath();
        }
        return includeSources;
    }

    public IIncludeStylesheet[] getIncludeStylesheet()
    {
        if ( includeStylesheets == null )
        {
            return null;
        }

        IIncludeStylesheet[] is = new IIncludeStylesheet[includeStylesheets.length];
        for ( int i = 0; i < includeStylesheets.length; i++ )
        {
            final MavenIncludeStylesheet ss = includeStylesheets[i];
            is[i] = new IIncludeStylesheet()
            {
                public String name()
                {
                    if ( ss.getName() != null )
                    {
                        return ss.getName();
                    }

                    return PathUtil.file( ss.getPath(), getResourcesTargetDirectories() ).getName();
                }

                public String path()
                {
                    return PathUtil.file( ss.getPath(), getResourcesTargetDirectories() ).getAbsolutePath();
                }
            };
        }

        return is;
    }

    @Override
    public String[] getLocale()
    {
        String[] locale = super.getLocale();
        if ( locale != null )
        {
            return locale;
        }

        return new String[] {};
    }

    @Override
    public String getProjectType()
    {
        return SWC;
    }

    public String getRoot()
    {
        return root;
    }

}

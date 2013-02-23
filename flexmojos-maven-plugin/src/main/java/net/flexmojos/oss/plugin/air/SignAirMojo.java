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
package net.flexmojos.oss.plugin.air;

import com.adobe.air.Listener;
import com.adobe.air.Message;
import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.air.packager.FlexmojosAIRPackager;
import net.flexmojos.oss.plugin.utilities.FileInterpolationUtil;
import net.flexmojos.oss.util.PathUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.flexmojos.oss.plugin.common.FlexExtension.*;
import static net.flexmojos.oss.util.PathUtil.file;
import static net.flexmojos.oss.util.PathUtil.path;

/**
 * @goal sign-air
 * @phase package
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class SignAirMojo
    extends AbstractMavenMojo
{

    private static String TIMESTAMP_NONE = "none";

    /**
     * @parameter default-value="${project.build.directory}/air"
     */
    private File airOutput;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     *
     * @parameter expression="${flexmojos.classifier}"
     */
    private String classifier;

    /**
     * @parameter default-value="${basedir}/src/main/resources/descriptor.xml"
     * @required
     */
    private File descriptorTemplate;

    /**
     * Additional properties to substitute into the air descriptor
     * @parameter
     */
    private Map<String, String> descriptorTemplateProperties;

    /**
     * Ideally Adobe would have used some parseable token, not a huge pass-phrase on the descriptor output. They did
     * prefer to reinvent wheel, so more work to all of us.
     *
     * @parameter expression="${flexmojos.flexbuilderCompatibility}"
     */
    private boolean flexBuilderCompatibility;

    /**
     * Include specified files in AIR package.
     *
     * @parameter
     */
    private List<String> includeFiles;

    /**
     * Include specified files or directories in AIR package.
     *
     * @parameter
     */
    private FileSet[] includeFileSets;

    /**
     * @parameter default-value="${basedir}/src/main/resources/sign.p12"
     */
    private File keystore;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @parameter
     * @required
     */
    private String storepass;

    /**
     * The type of keystore, determined by the keystore implementation.
     *
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * Strip artifact version during copy of dependencies.
     *
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * The URL for the timestamp server. If 'none', no timestamp will be used.
     *
     * @parameter
     */
    private String timestampURL;

    private void addSourceWithPath( FlexmojosAIRPackager packager, File directory, String includePath )
        throws MojoFailureException
    {
        if ( includePath == null )
        {
            throw new MojoFailureException( "Cannot include a null file" );
        }

        // get file from output directory to allow filtered resources
        File includeFile = new File( directory, includePath );
        if ( !includeFile.isFile() )
        {
            throw new MojoFailureException( "Include files only accept files as parameters: " + includePath );
        }

        // don't include the app descriptor or the cert
        if ( path( includeFile ).equals( path( this.descriptorTemplate ) )
            || path( includeFile ).equals( path( this.keystore ) ) )
        {
            return;
        }

        getLog().debug( "  adding source " + includeFile + " with path " + includePath );
        packager.addSourceWithPath( includeFile, includePath );
    }

    private void appendArtifacts( FlexmojosAIRPackager packager, Collection<Artifact> deps )
    {
        for ( Artifact artifact : deps )
        {
            if ( SWF.equals( artifact.getType() ) )
            {
                File source = artifact.getFile();
                String path = source.getName();
                if ( stripVersion && path.contains( artifact.getVersion() ) )
                {
                    path = path.replace( "-" + artifact.getVersion(), "" );
                }
                getLog().debug( "  adding source " + source + " with path " + path );
                packager.addSourceWithPath( source, path );
            }
        }
    }

    protected void doPackage( String packagerName, FlexmojosAIRPackager packager )
        throws MojoExecutionException
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance( storetype );
            keyStore.load( new FileInputStream( keystore.getAbsolutePath() ), storepass.toCharArray() );
            String alias = keyStore.aliases().nextElement();
            PrivateKey key = (PrivateKey) keyStore.getKey( alias, storepass.toCharArray() );
            packager.setPrivateKey( key );

            String c = this.classifier == null ? "" : "-" + this.classifier;
            File output =
                new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + c + "." + packagerName );
            packager.setOutput( output );
            packager.setDescriptor( getAirDescriptor() );

            Certificate certificate = keyStore.getCertificate( alias );
            packager.setSignerCertificate( certificate );
            Certificate[] certificateChain = keyStore.getCertificateChain( alias );
            packager.setCertificateChain( certificateChain );
            if ( this.timestampURL != null )
            {
                packager.setTimestampURL( TIMESTAMP_NONE.equals( this.timestampURL ) ? null : this.timestampURL );
            }

            String packaging = project.getPackaging();
            if ( AIR.equals( packaging ) )
            {
                appendArtifacts( packager, project.getDependencyArtifacts() );
                appendArtifacts( packager, project.getAttachedArtifacts() );
            }
            else if ( SWF.equals( packaging ) )
            {
                File source = project.getArtifact().getFile();
                String path = source.getName();
                getLog().debug( "  adding source " + source + " with path " + path );
                packager.addSourceWithPath( source, path );
            }
            else
            {
                throw new MojoFailureException( "Unexpected project packaging " + packaging );
            }

            if ( includeFiles == null && includeFileSets == null )
            {
                includeFileSets = resources.toArray( new FileSet[0] );
            }

            if ( includeFiles != null )
            {
                for ( final String includePath : includeFiles )
                {
                    File directory = file( project.getBuild().getOutputDirectory() );
                    addSourceWithPath( packager, directory, includePath );
                }
            }

            if ( includeFileSets != null )
            {
                for ( FileSet set : includeFileSets )
                {
                    DirectoryScanner scanner;
                    if ( set instanceof Resource )
                    {
                        scanner = scan( (Resource) set );
                    }
                    else
                    {
                        scanner = scan( set );
                    }

                    File directory = file( set.getDirectory(), project.getBasedir() );

                    String[] files = scanner.getIncludedFiles();
                    for ( String path : files )
                    {
                        addSourceWithPath( packager, directory, path );
                    }
                }
            }

            if ( classifier != null )
            {
                projectHelper.attachArtifact( project, packagerName, classifier, output );
            }
            else if ( SWF.equals( packaging ) )
            {
                projectHelper.attachArtifact( project, packagerName, output );
            }
            else
            {
                if ( AIR.equals( packagerName ) && AIR.equals( packaging ) )
                {
                    project.getArtifact().setFile( output );
                }
                else
                {
                    projectHelper.attachArtifact( project, packagerName, output );
                }
            }

            final List<Message> messages = new ArrayList<Message>();

            try
            {
                packager.setListener( new Listener()
                {
                    public void message( final Message message )
                    {
                        messages.add( message );
                    }

                    public void progress( final int soFar, final int total )
                    {
                        getLog().info( "  completed " + soFar + " of " + total );
                    }
                } );
            }
            catch ( NullPointerException e )
            {
                // this is a ridiculous workaround, but I have no means to prevent the NPE nor to check if it will
                // happen on AIR 2.5
                if ( getLog().isDebugEnabled() )
                {
                    getLog().error( e.getMessage() );
                }
            }

            packager.createPackage();

            if ( messages.size() > 0 )
            {
                for ( final Message message : messages )
                {
                    getLog().error( "  " + message.errorDescription );
                }

                throw new MojoExecutionException( "Error creating AIR application" );
            }
            else
            {
                getLog().info( "  AIR package created: " + output.getAbsolutePath() );
            }
        }
        catch ( MojoExecutionException e )
        {
            // do not handle
            throw e;
        }
        catch ( Exception e )
        {
            if ( getLog().isDebugEnabled() )
            {
                getLog().error( e.getMessage(), e );
            }
            throw new MojoExecutionException( "Error invoking AIR api", e );
        }
        finally
        {
            packager.close();
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        doPackage( AIR, new FlexmojosAIRPackager() );
    }

    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = getOutput();

        String version;
        if ( project.getArtifact().isSnapshot() )
        {
            version =
                project.getVersion().replace( "SNAPSHOT", new SimpleDateFormat( "yyyyMMdd.HHmmss" ).format( new Date() ) );
        }
        else
        {
            version = project.getVersion();
        }

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            ConcurrentMap<String, String> props = getDescriptorProperties();
            props.putIfAbsent("output", output.getName());
            props.putIfAbsent("version", version);

            FileInterpolationUtil.copyFile( descriptorTemplate, dest, props );

            if ( flexBuilderCompatibility )
            {
                // Workaround Flexbuilder/Flashbuilder weirdness
                String str = FileUtils.fileRead( dest );
                str =
                    str.replace( "[This value will be overwritten by Flex Builder in the output app.xml]",
                                 output.getName() );
                str =
                    str.replace( "[This value will be overwritten by Flash Builder in the output app.xml]",
                                 output.getName() );
                FileUtils.fileWrite( PathUtil.path( dest ), str );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy air template", e );
        }

        return dest;
    }

    private ConcurrentMap<String, String> getDescriptorProperties() {
        final ConcurrentHashMap<String, String> templateProps = new ConcurrentHashMap<String, String>();
        if (descriptorTemplateProperties != null) {
            templateProps.putAll(descriptorTemplateProperties);
        }
        return templateProps;
    }

    private File getOutput()
    {
        File output = null;
        if ( project.getPackaging().equals( AIR ) )
        {
            List<Artifact> attach = project.getAttachedArtifacts();
            for ( Artifact artifact : attach )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
            Set<Artifact> deps = project.getDependencyArtifacts();
            for ( Artifact artifact : deps )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
        }
        else
        {
            output = project.getArtifact().getFile();
        }
        return output;
    }

}

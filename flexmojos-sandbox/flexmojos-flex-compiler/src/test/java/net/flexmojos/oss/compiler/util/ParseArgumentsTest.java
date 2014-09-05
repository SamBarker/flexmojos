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
package net.flexmojos.oss.compiler.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static net.flexmojos.oss.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.DefaultPlexusContainer;
import net.flexmojos.oss.compiler.ICompcConfiguration;
import net.flexmojos.oss.compiler.ICompilerConfiguration;
import net.flexmojos.oss.compiler.IDefine;
import net.flexmojos.oss.compiler.IFontsConfiguration;
import net.flexmojos.oss.compiler.IFrame;
import net.flexmojos.oss.compiler.IFramesConfiguration;
import net.flexmojos.oss.compiler.ILanguageRange;
import net.flexmojos.oss.compiler.ILanguages;
import net.flexmojos.oss.compiler.IMetadataConfiguration;
import net.flexmojos.oss.compiler.INamespace;
import net.flexmojos.oss.compiler.INamespacesConfiguration;
import net.flexmojos.oss.compiler.IRuntimeSharedLibraryPath;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ParseArgumentsTest
{

    private FlexCompilerArgumentParser parser;

    @BeforeClass
    public void createParser()
        throws Exception
    {
        DefaultPlexusContainer plexus = new DefaultPlexusContainer();
        parser = plexus.lookup( FlexCompilerArgumentParser.class );
    }

    @Test
    public void simpleCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getDebugPassword() ).thenReturn( "dbgPw" );

        List<String> args = parser.getArgumentsList( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertEquals( args.get( 0 ), "-debug-password=dbgPw" );
    }

    @Test
    public void compilerCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        IFontsConfiguration fontCfg = mock( IFontsConfiguration.class, RETURNS_NULL );
        ILanguages langsCfg = mock( ILanguages.class, RETURNS_NULL );
        ILanguageRange thaiLangRangeCfg = mock( ILanguageRange.class, RETURNS_NULL );
        ILanguageRange ptLangRangeCfg = mock( ILanguageRange.class, RETURNS_NULL );
        IMetadataConfiguration metadataCfg = mock( IMetadataConfiguration.class, RETURNS_NULL );
        IRuntimeSharedLibraryPath runtimeCfg = mock( IRuntimeSharedLibraryPath.class, RETURNS_NULL );
        Map<String, String> urls = new LinkedHashMap<String, String>();
        urls.put( "http://a.com/rsls/MyLibrary.swf", "http://a.com/rsls/crossdomain.xml" );
        urls.put( "MyLibrary.swf", null );
        IFramesConfiguration frameCfg = mock( IFramesConfiguration.class, RETURNS_NULL );
        IFrame frame = mock( IFrame.class, RETURNS_NULL );
        INamespacesConfiguration namespacesCfg = mock( INamespacesConfiguration.class, RETURNS_NULL );
        INamespace namespace = mock( INamespace.class, RETURNS_NULL );
        INamespace namespace2 = mock( INamespace.class, RETURNS_NULL );
        IDefine define = mock( IDefine.class, RETURNS_NULL );

        when( cfg.getIncludeClasses() ).thenReturn( Arrays.asList( "AClass", "BClass" ) );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( cfg.getMetadataConfiguration() ).thenReturn( metadataCfg );
        when( compilerCfg.getAccessible() ).thenReturn( true );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontCfg );
        when( fontCfg.getLanguagesConfiguration() ).thenReturn( langsCfg );
        when( langsCfg.getLanguageRange() ).thenReturn( new ILanguageRange[] { thaiLangRangeCfg, ptLangRangeCfg } );
        when( thaiLangRangeCfg.lang() ).thenReturn( "Thai" );
        when( thaiLangRangeCfg.range() ).thenReturn( "U+0E01-0E5B" );
        when( ptLangRangeCfg.lang() ).thenReturn( "ptBR" );
        when( ptLangRangeCfg.range() ).thenReturn( "U+0A0C-0EAA" );
        when( metadataCfg.getCreator() ).thenReturn( new String[] { "Marvin", "VELO", "Froeder" } );
        when( cfg.getRuntimeSharedLibraryPath() ).thenReturn( new IRuntimeSharedLibraryPath[] { runtimeCfg } );
        when( runtimeCfg.pathElement() ).thenReturn( "MyLibrary.swc" );
        when( runtimeCfg.rslUrl() ).thenReturn( urls );
        when( cfg.getFramesConfiguration() ).thenReturn( frameCfg );
        when( frameCfg.getFrame() ).thenReturn( new IFrame[] { frame } );
        when( frame.label() ).thenReturn( "my-frame" );
        when( frame.classname() ).thenReturn( new String[] { "org.package.1", "org.package.2" } );
        when( compilerCfg.getNamespacesConfiguration() ).thenReturn( namespacesCfg );
        when( namespacesCfg.getNamespace() ).thenReturn( new INamespace[] { namespace, namespace2 } );
        when( namespace.uri() ).thenReturn( "http://www.adobe.com/2006/mxml" );
        when( namespace.manifest() ).thenReturn( "mx-manifest.xml" );
        when( namespace2.uri() ).thenReturn( "library://ns.adobe.com/flex/spark" );
        when( namespace2.manifest() ).thenReturn( "spark-manifest.xml" );
        when( compilerCfg.getDefine() ).thenReturn( new IDefine[] { define } );
        when( define.name() ).thenReturn( "CFG::AAA" );
        when( define.value() ).thenReturn( "true" );

        List<String> args = parser.getArgumentsList( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 21, args.toString() );
        Assert.assertTrue( args.contains( "-include-classes=AClass,BClass" ) );
        Assert.assertTrue( args.contains( "-compiler.accessible=true" ) );
        Assert.assertTrue( args.contains( "-metadata.creator=Marvin" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=VELO" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=Froeder" ) );
        assertThat( args, ArrayMatcher.subArray( "-compiler.fonts.languages.language-range", "Thai", "U+0E01-0E5B" ) );
        assertThat( args, ArrayMatcher.subArray( "-compiler.fonts.languages.language-range", "ptBR", "U+0A0C-0EAA" ) );
        assertThat( args,
                    ArrayMatcher.subArray( "-runtime-shared-library-path=MyLibrary.swc,http://a.com/rsls/MyLibrary.swf,http://a.com/rsls/crossdomain.xml,MyLibrary.swf" ) );
        assertThat( args, ArrayMatcher.subArray( "-frames.frame", "my-frame", "org.package.1", "org.package.2" ) );
        assertThat( args, ArrayMatcher.subArray( "-compiler.namespaces.namespace=http://www.adobe.com/2006/mxml,mx-manifest.xml" ) );
        assertThat( args, ArrayMatcher.subArray( "-compiler.namespaces.namespace=library://ns.adobe.com/flex/spark,spark-manifest.xml" ) );
        assertThat( args, ArrayMatcher.subArray( "-compiler.define", "CFG::AAA", "true" ) );
    }

    @Test
    public void emptyCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );

        List<String> args = parser.getArgumentsList( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertTrue( args.contains( "-load-config=" ) );
    }
}

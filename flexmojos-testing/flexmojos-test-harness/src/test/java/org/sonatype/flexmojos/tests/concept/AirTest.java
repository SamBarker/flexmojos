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
package org.sonatype.flexmojos.tests.concept;

import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.util.OSUtils;
import org.testng.annotations.Test;

public class AirTest
    extends AbstractConceptTest
{

    @Test
    public void airApp()
        throws Exception
    {
        standardConceptTester( "simple-air" );
    }

    @Test
    public void simplify()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "simplify-air" );

        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "air" );
    }

    @Test
    public void nativeAir()
        throws Exception
    {
        String osPackages;
        if ( OSUtils.isLinux() )
        {
            osPackages = "deb";
        }
        else if ( OSUtils.isMacOS() )
        {
            osPackages = "dmg";
        }
        else
        {
            osPackages = "exe";
        }

        FMVerifier v = standardConceptTester( "native-air", "-Dos.packages=" + osPackages );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "air" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", osPackages );
    }
}

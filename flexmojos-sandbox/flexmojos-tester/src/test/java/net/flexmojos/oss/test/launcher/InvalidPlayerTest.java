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
package net.flexmojos.oss.test.launcher;

import static org.testng.Assert.fail;

import org.codehaus.plexus.context.Context;
import net.flexmojos.oss.test.TestRequest;
import net.flexmojos.oss.test.ThreadStatus;
import org.testng.annotations.Test;

public class InvalidPlayerTest
    extends AbstractAsVmLauncherTest
{

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "flashplayer.command", "invalid_flash_player" );
    }

    @Test( timeOut = 20000, enabled = false )
    public void invalidPlayer()
        throws Exception
    {
        TestRequest request = new TestRequest();
        request.setSwf( VALID_SWF.getSwf() );
        request.setFlashplayerCommand( new String[] { "invalid_flash_player" } );

        if ( launcher.useXvfb() )
        {
            launcher.start( request );

            do
            {
                Thread.yield();
                Thread.sleep( 100 );
            }
            while ( !ThreadStatus.ERROR.equals( launcher.getStatus() ) );

            System.out.println( launcher.getConsoleOutput() );
        }
        else
        {
            try
            {
                launcher.start( request );

                fail( launcher.getConsoleOutput() );
            }
            catch ( LaunchFlashPlayerException e )
            {
                // expected
            }
            finally
            {
                launcher.stop();
            }
        }
    }

}

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
package net.flexmojos.oss.test.monitor;

import static net.flexmojos.oss.test.monitor.CommConstraints.EOL;
import static net.flexmojos.oss.test.monitor.CommConstraints.FINISHED;
import static net.flexmojos.oss.test.monitor.CommConstraints.OK;
import static net.flexmojos.oss.test.monitor.CommConstraints.STATUS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import net.flexmojos.oss.test.ThreadStatus;

/**
 * This class will ping Action Script virtual machine to make sure if the application still running
 * 
 * @author velo
 */
@Component( role = AsVmPing.class, instantiationStrategy = "per-lookup" )
public class AsVmPing
    extends AbstractSocketThread
{

    private int testControlPort;

    private int firstConnectionTimeout;

    private int testTimeout;

    @Override
    protected void handleRequest()
        throws SocketTimeoutException, SocketException, IOException
    {
        getLogger().debug( "[CONTROL] AsVmControl handleRequest" );

        clientSocket.setSoTimeout( testTimeout );

        int errorCount = 0;

        while ( true )
        {
            try
            {
                getLogger().debug( "[CONTROL] query status" );
                IOUtil.copy( STATUS + EOL, out );

                getLogger().debug( "[CONTROL] received status" );
                BufferedReader in = new BufferedReader( new InputStreamReader( super.in ) );
                String result = in.readLine();
                getLogger().debug( "[CONTROL] status is: " + result );
                if ( !OK.equals( result ) && !FINISHED.equals( result ) )
                {
                    errorCount++;
                    if ( errorCount >= 3 )
                    {
                        status = ThreadStatus.ERROR;
                        error = new Error( "Invalid virtual machine status: " + result );

                        return;
                    }
                }
                else if ( FINISHED.equals( result ) )
                {
                    getLogger().debug( "[CONTROL] FINISHED received, terminating the thread" );
                    return;
                }
                else
                {
                    errorCount = 0;

                    try
                    {
                        Thread.sleep( 2000 );
                    }
                    catch ( InterruptedException e )
                    {
                        // wake up call, no problem
                    }
                }
            }
            catch ( SocketException e )
            {
                errorCount++;
                if ( errorCount >= 3 )
                {
                    status = ThreadStatus.ERROR;
                    error = e;

                    return;
                }
            }
        }
    }

    public void start(int testControlPort, int firstConnectionTimeout, int testTimeout)
    {
        reset();
        this.testControlPort = testControlPort;
        this.firstConnectionTimeout = firstConnectionTimeout;
        this.testTimeout = testTimeout;
        launch();
    }

    @Override
    protected int getTestPort()
    {
        return testControlPort;
    }

    @Override
    protected int getFirstConnectionTimeout()
    {
        return firstConnectionTimeout;
    }

}

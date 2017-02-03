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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import net.flexmojos.oss.test.AbstractControlledThread;
import net.flexmojos.oss.test.ControlledThread;
import net.flexmojos.oss.test.ThreadStatus;

public abstract class AbstractSocketThread
    extends AbstractControlledThread
    implements ControlledThread
{

    protected ServerSocket serverSocket;

    protected Socket clientSocket;

    protected InputStream in;

    protected OutputStream out;

    public int testPort;

    public AbstractSocketThread()
    {
        super();
    }

    public void run()
    {
        try
        {
            openServerSocket();
            status = ThreadStatus.STARTED;

            openClientSocket();

            handleRequest();

            if ( !ThreadStatus.ERROR.equals( status ) )
            {
                status = ThreadStatus.DONE;
            }
        }
        catch ( SocketTimeoutException e )
        {
            status = ThreadStatus.ERROR;
            error = e;
        }
        catch ( IOException e )
        {
            status = ThreadStatus.ERROR;
            error = e;
        }
        finally
        {
            // always stop the server loop
            closeClientSocket();
            closeServerSocket();
        }
    }

    protected abstract void handleRequest()
        throws SocketTimeoutException, SocketException, IOException;

    private void openServerSocket()
        throws IOException
    {
        serverSocket = new ServerSocket( getTestPort() );
        serverSocket.setSoTimeout( getFirstConnectionTimeout() );

        getLogger().debug( "[" + this.getClass().getName() + "] opened server socket on port " + getTestPort() );
    }

    protected abstract int getFirstConnectionTimeout();

    protected abstract int getTestPort();

    private void closeServerSocket()
    {
        if ( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    protected void openClientSocket()
        throws SocketException, IOException, SocketTimeoutException
    {
        // This method blocks until a connection is made.
        clientSocket = serverSocket.accept();

        // serverSocket.setSoTimeout( 0 );

        getLogger().debug( "[" + this.getClass().getName() + "] accepting data from client" );

        status = ThreadStatus.RUNNING;

        in = clientSocket.getInputStream();
        out = clientSocket.getOutputStream();
    }

    protected void closeClientSocket()
    {
        // Close the output stream.
        if ( out != null )
        {
            try
            {
                out.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }

        // Close the input stream.
        if ( in != null )
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }

        // Close the client socket.
        if ( clientSocket != null )
        {
            try
            {
                clientSocket.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public void stop()
    {
        try
        {
            if ( this.serverSocket != null )
            {
                this.serverSocket.close();
            }
        }
        catch ( IOException e )
        {
            getLogger().debug( e.getMessage(), e );
        }
    }

    @Override
    protected void reset()
    {
        super.reset();

        this.serverSocket = null;
        this.clientSocket = null;
        this.in = null;
        this.out = null;
    }

}
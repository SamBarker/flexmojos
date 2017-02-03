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
package net.flexmojos.oss.test.report;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@SuppressWarnings( "unused" )
public class TestCaseReport
{

    private List<TestCoverageReport> coverage;

    private Xpp3Dom dom;

    private int errors;

    private int failures;

    private List<TestMethodReport> methods;

    private String name;

    private int tests;

    private double time;

    public TestCaseReport( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public List<TestCoverageReport> getCoverage()
    {
        if ( this.coverage == null )
        {
            this.coverage = new ArrayList<TestCoverageReport>();
            for ( Xpp3Dom child : dom.getChildren( "coverage" ) )
            {
                coverage.add( new TestCoverageReport( child ) );
            }
        }
        return coverage;
    }

    public int getErrors()
    {
        return Integer.parseInt( dom.getAttribute( "errors" ) );
    }

    public int getFailures()
    {
        return Integer.parseInt( dom.getAttribute( "failures" ) );
    }

    public List<TestMethodReport> getMethods()
    {
        if ( this.methods == null )
        {
            this.methods = new ArrayList<TestMethodReport>();
            for ( Xpp3Dom child : dom.getChildren( "testcase" ) )
            {
                methods.add( new TestMethodReport( child ) );
            }
        }
        return this.methods;
    }

    public String getName()
    {
        return dom.getAttribute( "name" );
    }

    public int getTests()
    {
        return Integer.parseInt( dom.getAttribute( "tests" ) );
    }

    public double getTime()
    {
        return Double.parseDouble( dom.getAttribute( "time" ) );
    }

    public void setCoverage( List<TestCoverageReport> coverage )
    {
        throw new UnsupportedOperationException();
    }

    public void setErrors( int errors )
    {
        throw new UnsupportedOperationException();
    }

    public void setFailures( int failures )
    {
        throw new UnsupportedOperationException();
    }

    public void setMethods( List<TestMethodReport> methods )
    {
        throw new UnsupportedOperationException();
    }

    public void setName( String name )
    {
        throw new UnsupportedOperationException();
    }

    public void setTests( int tests )
    {
        throw new UnsupportedOperationException();
    }

    public void setTime( double time )
    {
        throw new UnsupportedOperationException();
    }

}

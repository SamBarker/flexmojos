/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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
package net.flexmojos.oss.unitestingsupport
{
	import flash.desktop.NativeApplication;

    import mx.core.WindowedApplication;
    import mx.events.FlexEvent;

    public class TestAirApplication extends WindowedApplication implements ITestApplication
	{

		private var _componentUnderTest:*;

		private var _tests:Array;

		private static var socketReporter:SocketReporter=SocketReporter.getInstance();

		private static var controlSocket:ControlSocket=ControlSocket.getInstance();

		public function set port(port:int):void
		{
			socketReporter.port=port;
		}

		public function set controlPort(port:int):void
		{
			controlSocket.port=port;
		}

		public function TestAirApplication()
		{
			this._tests=[];

            addEventListener( FlexEvent.CREATION_COMPLETE, runTests );
		}

		private function runTests(e:*):void
		{
			controlSocket.connect(this);
			socketReporter.runTests(this);
		}

		/**
		 * Test to be run
		 * @param test the Test to run.
		 */
		public function addTest(test:Class):void
		{
			_tests.push(test);
			trace("Testing " + test);
		}

		public function get tests():Array
		{
			return this._tests;
		}

		public function get componentUnderTest():*
		{
			return _componentUnderTest;
		}

		public function set componentUnderTest(cmp:*):void
		{
			if (this._componentUnderTest != null)
			{
				removeChild(this._componentUnderTest);
				this._componentUnderTest=null;
			}

			if (cmp != null)
			{
				this._componentUnderTest=cmp;
				addChild(this._componentUnderTest);
			}
		}

		public function killApplication():void
		{
			NativeApplication.nativeApplication.exit()
		}

	}
}
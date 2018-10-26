
/******************************************************************************************************************
* File:Plumber.java
* Course: Software Architecture
* Project: System A
* Versions:
*	1.0 November 2016 - SystemA Pipe and Filter code.
*
* Description:
* 
* This class creates a main thread to instantiate and connect a set of filters. The class works same as the one in example to create the source and sink filter
* from which the data originates and terminates.
*
* Parameters: 		None
*
* Internal Methods:	None
* @author Krishna
******************************************************************************************************************/

public class PlumberSysA
{
	public static void main(String args[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilterA Filter1 = new SourceFilterA();
		MiddleFilterA Filter2 = new MiddleFilterA();
		SinkFilterA Filter3 = new SinkFilterA();

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/

		Filter3.Connect(Filter2); // This esstially says, "connect Filter3 input port to Filter2 output port
		Filter2.Connect(Filter1); // This esstially says, "connect Filter2 intput port to Filter1 output port

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		Filter1.start();
		Filter2.start();
		Filter3.start();

   } // main

} //
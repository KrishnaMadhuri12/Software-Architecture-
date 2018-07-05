/*************************************************************
* File:MiddleFilter.java
* Course: Software Architecture
* Project: System A
* Versions:
*	1.0 November 2016 
*
* Description:
* This class serves as middle ware -- connector to transfer the
* information from source filter to sink filter.
* @author Krishna
**************************************************************/

public class MiddleFilterA extends FilterFrameworkA{
	public void run()
    {


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Middle Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{
				databyte = ReadFilterInputPort();
				bytesread++;
				WriteFilterOutputPort(databyte);
				byteswritten++;

			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run
}

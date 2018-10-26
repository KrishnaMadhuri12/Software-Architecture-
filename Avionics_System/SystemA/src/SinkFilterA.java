
/*************************************************************
* File:SinkFilterA.java
* Course: Software Architecture
* Project: System A
* Versions:
*	1.0 November 2016 - SystemA SinkFilter code
*
* Description:
* This class reads the data passed through middle filter and process the 
* information respective to the functionality required.
* Required functionality is to convert temperature measurements 
* from Fahrenheit to celsius and altiude from feet to meters.
* 
* @author Krishna
**************************************************************/



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SinkFilterA extends FilterFrameworkA{
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id which we use to check for temperature and altitude
		int i;							// This is a loop counter
		
		double altitude_list[] = new double[100];		//This is the arraylist used to store altitude information
		double temperature_list[]=new double[100];		//This is the arraylist used to store temperature information
		String time_list[]= new String[100];			//This is the arraylist used to store timeStamp information
		
		int alt_count=0;								//This is the increment counter for altitudelist array
		int temp_count = 0;								//This is the increment counter for temperatureList array
		int time_count=0;								//This is the increment counter for timeStamp_list array
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading \n");
		
		while (true)
		{
			try
			{
				/***************************************************************************
				// We know that the first data coming to this filter is going to be an ID and
				// that it is IdLength long. So we first decommutate the ID bytes.
				****************************************************************************/

				id = 0;
				
				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for

				/****************************************************************************
				// Here we read measurements. All measurement data is read as a stream of bytes
				// and stored as a long value. This permits us to do bitwise manipulation that
				// is neccesary to convert the byte stream into data words. Note that bitwise
				// manipulation is not permitted on any kind of floating point types in Java.
				// If the id = 0 then this is a time value and is therefore a long value - no
				// problem. However, if the id is something other than 0, then the bits in the
				// long value is really of type double and we need to convert the value using
				// Double.longBitsToDouble(long val) to do the conversion which is illustrated.
				// below.
				*****************************************************************************/

				measurement = 0;
				

				for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort();
					measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

					if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
					{												// previously appended byte to the left by one byte
						measurement = measurement << 8;				// to make room for the next byte we append to the
																	// measurement
					} // if

					bytesread++;									// Increment the byte count

				} // if

				/**
				 * Here we are looking for the time with measurement id=0 in the source file
				 */

				if ( id == 0 )
				{
					TimeStamp.setTimeInMillis(measurement);
					
					time_list[time_count]=TimeStampFormat.format(TimeStamp.getTime());
					time_count++;
				} // if
				/**
				 * Looking for the altitude id=2in each frame and 
				 * converting the altitude from feet to meters by multiplying with *0.3048
				 */
				if(id==2)
				{
					altitude_list[alt_count]=(Double.longBitsToDouble(measurement)*0.3048);
					alt_count++;
				}//if
				

				/**
				 * Looking of temperature id=4 in source
				 * Converting ~F to ~C.
				 */
				
				if ( id == 4 )
				{
					
					temperature_list[temp_count]=(int) ((Double.longBitsToDouble(measurement)-32)*0.5555555);
					temp_count++;
				} // if
			
				
				
				
			} // try

			/*******************************************************************************
			*	The EndOfStreamExeception below is thrown when you reach end of the input
			*	stream (duh). At this point, the filter ports are closed and a message is
			*	written letting the user know what is going on.
			********************************************************************************/

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread);
				try {
					/**
					 * output is the Data S
					 */
					 PrintWriter output = new PrintWriter ("OuputA.txt");
					 output.print("TimeStamp                   Altitude              Temperature");
					for(int increment=0;increment<time_list.length;increment++)
					{
						record(output,time_list[increment],altitude_list[increment],temperature_list[increment]);
					}//for
					
					
					output.close();
				} 
				/*******************************************************************************
				*	The FileNotFoundException is thrown by PrintWriter while trying to create a file 
				********************************************************************************/
				catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}//catch
				catch (IOException e1) {
					
					e1.printStackTrace();
				}//catch
				
				break;

			} // catch
			
			

		} // while

   }
	/**
	 * Method to write data into the text file 
	 * @param f
	 * @param time
	 * @param altitude
	 * @param temperature
	 * @throws IOException
	 */
		static void record( PrintWriter f, String time,double altitude, double temperature) throws IOException{
		f.println();
		f.print(time);
		f.print("     ");
		f.print(altitude);
		f.print("      ");
		f.print(temperature);
		
		
		
		}//record
		
	} // run

//SinkFilterA

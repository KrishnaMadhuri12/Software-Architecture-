/******************************************************************************************************************
* File:SinkFilterSysB.java
* Course: Software Architecture
* Project: System B
* Versions:
*	1.0 November 2016 - SinkFilter for system B
*
* Description:
*
* This class reads the data bytes from middle filter and functions teh following responsibilities;
* 
* 1)Conversion of altitude from feet to meters
* 2)Temperature measurements from Fahrenheit to Celsius
* 3)Filter "Wild Jumps" out of the data stream for alitude 
* where the wild jump  is a variation of more than 100m between two adjacent frames
* 
* Wild Jump encountered in the stream, interpolate a replacement value by computing
* the average of the last valid measurement and the next vali measurement in the stream.
* 
* If in case the wild jump occurs at the end of the stream,
* replace it with the last valid value
* 
* @author Krishna
******************************************************************************************************************/


import java.util.*;						// This class is used to interpret time words
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class SinkFilterSysB extends FilterFrameworkSysB
{
	
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
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		
		double altitude_list[] = new double[100];		//This is the arraylist used to store altitude information
		double wildjump_list[]=new double[100];			//This is the arraylist used to store wildjump information
		double temperature_list[]=new double[100];		//This is the arraylist used to store temperature information
		double pressure_list[]=new double[100];			//This is the arraylist used to store pressure information
		String time_list[]= new String[100]; 			//This is the arraylist used to store time information
		
		int altitude_count=0;							//Array incrementer for altitude
		int presure_count=0;
		int wildjump_count=0;
		int temperature_count = 0;
		int time_count=0;
		
				
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading ");

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

				} // for
				
				/**
				 * Here we are looking for the time with measurement id=0 in the source file
				 */

				if ( id == 0 )
				{
					TimeStamp.setTimeInMillis(measurement);
					System.out.print("Time stamp"+ TimeStampFormat.format(TimeStamp.getTime()));
					time_list[time_count]=TimeStampFormat.format(TimeStamp.getTime());
				}
				/**
				 * Here we are looking for the altitude with measurement id=2 in the source file
				 * and converting it into meters
				 */
				
				if(id==2)
				{
					altitude_list[altitude_count]=Double.longBitsToDouble(measurement)*0.3048;// Code to convert altitude into meters
					altitude_count++;
					
				}
				
				/**
				 * Here we are looking for the pressure with measurement id=3 in the source file
				 */
				if(id==3)
				{
				pressure_list[presure_count]=Double.longBitsToDouble(measurement); //Gets the pressure measure storing into an array 
				presure_count++;											  //list and incrementing the array index
				}
				
				/**
				 * Here we are looking for the temperature with measurement id=2 in the source file
				 * And converting it into celsius scale
				 */
				if(id==4)
				{
				temperature_list[temperature_count]=(Double.longBitsToDouble(measurement)-32)*0.5555555;// Code to convert fahrenheit to celsius
				temperature_count++;
				}
				
				
				
				
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
				/**
				 * We check the wild jumps for altitude with 100m difference. 
				 * 
				 */
	
				int c=0;				//Variable to increment the array list index
				if(c==0)				//Checking for array index 0
				{
					//Below print statement is used to display output in console file.
					//System.out.println(" Altitude Value :"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					wildjump_list[wildjump_count]=altitude_list[c];
					wildjump_count++;
				}//if
				/**
				 * for loop to increment the index value to check the store values
				 */
				for(c=1;c<altitude_count-1;c++)
				{
					if(altitude_list[c]-altitude_list[c-1]>100)		//Checking the condition for wild jump
					{
						wildjump_list[wildjump_count]=altitude_list[c];		//Storing the rejected value in a wild_list array
						wildjump_count++;
						altitude_list[c]=(altitude_list[c-1]+altitude_list[c+1])/2;	//Interpolated value into the wild jump entry
						System.out.println(" altitude with wild jump"+altitude_list[c]+" Pressure value"+pressure_list[c]+" Temperature value:"+temperature_list[c]);
					}//if
					
					else
					{
						System.out.println(" altitude without wild jump"+altitude_list[c]+" Pressure value"+pressure_list[c]+" Temperature value:"+temperature_list[c]);
					}//else
					
				}//for
				if(c==altitude_count-1)
				{
					if(altitude_list[c]-altitude_list[c-1]>100)
					{
						wildjump_list[wildjump_count]=altitude_list[c];
						altitude_list[c]=altitude_list[c-1];
						System.out.println(" Last value of altitude is wild jump"+altitude_list[c]+" Pressure value"+pressure_list[c]+" Temperature value:"+temperature_list[c]);
					}
					else{
						System.out.println(" Last value of altitude without wild jump"+altitude_list[c]+" Pressure value"+pressure_list[c]+" Temperature value:"+temperature_list[c]);
					}
				}
				/**
				 * Now that wild jumps and wild point are calculated lets store them two diff data files
				 * 
				 */
				
				System.out.println("--------------------------------------------");
				System.out.println("Rejected altitude values are below:");
				for(int count=0;count<wildjump_count;count++)
				{
					System.out.println(wildjump_list[count]);
				}
				try{
				/**
				 * An output stream to create an dat file with the modified values on altitude.	
				 */
					 DataOutputStream output = new DataOutputStream(new FileOutputStream(
				                "OutputB.dat"));
					 DataOutputStream wildjump = new DataOutputStream(new FileOutputStream(
				                "WildPoints.dat"));
					for(int var=0; var<altitude_count;var++)		//var  is an increment variable created to traverse in the array
					{
					
					 output.writeDouble(altitude_list[var]);
					 output.writeDouble(pressure_list[var]);
					 output.writeDouble(temperature_list[var]);
					 wildjump.writeDouble(wildjump_list[var]);
					}
					output.close();
				}
				catch(IOException ioe){}
				
				
				
				
				break;
				

			} // catch

			
		} // while
	
	   } // run


} // SinKFilterSysB
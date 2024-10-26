package org.example;
// Name: Chenxuan Gao
// Student id: 300229429
/*
Explanation:
The logical running sequence should be:
Student -> Professor -> TA,
	 student only release professor semaphore,
	 professor release both student and TA semaphore,
	 TA only release student semaphore,
 Student and TA also exchange the shovel semaphore!
*/
//
// The Planting Synchronization Problem
//
import java.util.concurrent.Semaphore;

public class Planting
{
	public static void main(String args[]) 
	{
		int i;
	 	// Create Student, TA, Professor threads
		TA ta = new TA();
		Professor prof = new Professor(ta);
		Student stdnt = new Student(ta);

		// Start the threads
		prof.start();
		ta.start();
		stdnt.start();

		// Wait for prof to call it quits
		try {prof.join();} catch(InterruptedException e) { }; 
		// Terminate the TA and Student Threads
		ta.interrupt();
		stdnt.interrupt();
	}   
}

class Student extends Thread
{
	TA ta;

	public Student(TA taThread)
        {
	    ta = taThread;
	}

	public void run()
	{
		while(true)
		{
			try{
				// Check if student allow to dig
				ta.student.acquire();
				// Check if the shovel is available
				ta.availableShovel.acquire();
				// If more than MAX unfilled holes
				if((ta.getHoleDug() - ta.getHoleFilledNum()) >= ta.getMAX()){
					System.out.println("Student: Must wait for TA "+ta.getMAX()+" holes ahead");
					// Wait until get filled
					ta.availableShovel.release();
					continue;
				}
				// Can dig a hole - lets get the shovel
				System.out.println("Student: Got the shovel");
				// Digging...
				sleep((int) (100*Math.random()));
				// hole filled - increment the number
				ta.incrHoleDug();
				System.out.println("Student: Hole "+ta.getHoleDug()+" Dug");
				System.out.println("Student: Letting go of the shovel");
				// Allow professor to seed
				ta.professor.release();
				// Release the shovel
				ta.availableShovel.release();
			}catch (Exception e){
				break;
			}
			if(isInterrupted()) break;
		}
		System.out.println("Student is done");
	}
}

class TA extends Thread
{
	// Some variables to count number of holes dug and filled - the TA keeps track of things
	private int holeFilledNum=0;  // number of the hole filled
	private int holePlantedNum=0;  // number of the hole planted
	private int holeDugNum=0;     // number of hole dug
	private final int MAX=5;   // can only get 5 holes ahead
	// add semaphores - the professor lets the TA manage things.
	Semaphore availableShovel;
	Semaphore professor;
	Semaphore ta;
	Semaphore student;

	public int getMAX() { return(MAX); }
	public void incrHoleDug() { holeDugNum++; }
	public int getHoleDug() { return(holeDugNum); }
	public void incrHolePlanted() { holePlantedNum++; }
	public int getHolePlanted() { return(holePlantedNum); }

	public int getHoleFilledNum() {return holeFilledNum;}

	public TA()
	{
		// Only 1 shovel
		availableShovel = new Semaphore(1);
		// Professor should not start immediately
		professor = new Semaphore(0);
		// Professor should not start immediately
		ta = new Semaphore(0);
		// Allow student to dig max 5 holes
		student = new Semaphore(MAX + 1);
	}
	
	public void run()
	{
		while(true)
		{
			try{
				// Check if TA allow to fill
				ta.acquire();
				availableShovel.acquire();
				System.out.println("TA: Got the shovel");
				// Time to fill hole
				sleep((int) (100*Math.random()));
				// hole filled - increment the number
				holeFilledNum++;
				System.out.println("TA: The hole "+holeFilledNum+" has been filled");
				System.out.println("TA: Letting go of the shovel");
				// After filled, student are grant to run
				student.release();
				availableShovel.release();
			}catch (Exception e){
				break;
			}
		     if(isInterrupted()) break;
		}
		System.out.println("TA is done");
	}
}

class Professor extends Thread
{
	TA ta;

	public Professor(TA taThread)
        {
	    ta = taThread;
	}

	public void run()
	{
		while(ta.getHolePlanted() <= 20)
		{
			try{
				// Check if professor allow to seed
				ta.professor.acquire();
				// Time to plant
				sleep((int) (50*Math.random()));
				// the seed is planted - increment the number
				ta.incrHolePlanted();
				System.out.println("Professor: All be advised that I have completed planting hole "+ ta.getHolePlanted());
				// After seed, both TA and student can start filling or digging.
				ta.ta.release();
				ta.student.release();
			}catch (Exception e){
				break;
			}
		}
		System.out.println("Professor: We have worked enough for today");
	}
}

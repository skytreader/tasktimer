package net.skytreader.kode.tasktimer;

import java.awt.EventQueue;

public class TaskTimerMain{
	public static void main(String[] args){
		EventQueue.invokeLater(new TaskTimerRunnable());
	}
}

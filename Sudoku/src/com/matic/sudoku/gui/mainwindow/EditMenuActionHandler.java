/*
* This file is part of SuDonkey, an open-source Sudoku puzzle game generator and solver.
* Copyright (C) 2014 Vedran Matic
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/

package com.matic.sudoku.gui.mainwindow;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.util.Constants;

/**
 * An action handler for Edit-menu options
 * 
 * @author vedran
 *
 */
class EditMenuActionHandler implements ActionListener {
	
	private final MainWindow mainWindow;
	private final Board board;
	
	public EditMenuActionHandler(final MainWindow mainWindow, final Board board) {
		this.mainWindow = mainWindow;
		this.board = board;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		
		switch(actionCommand) {
		case MainWindow.COPY_STRING:
			handleCopyAction();
			break;
		case MainWindow.PASTE_STRING:
			handlePasteAction();
			break;	
		case MainWindow.CLEAR_COLORS_STRING:
			board.clearColorSelections();
			break;
		}
	}
		
	private void handleCopyAction() {
		final String puzzleAsString = board.asString();
		
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final StringSelection stringSelection = new StringSelection(puzzleAsString);
		
		clipboard.setContents(stringSelection, null);
	}		
	
	private void handlePasteAction() {
		final String clipboardContents = getClipboardContents();
		
		if(clipboardContents == null) {
			return;
		}	
		
		// Warn player about board contents being replaced
		if (board.isVerified()) {
			final String message = "Are you sure you want to replace board contents?";
			final String title = "Confirm replace";
			final int choice = JOptionPane.showConfirmDialog(mainWindow.window,
					message, title, JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		final int[] pastedPuzzle = convertClipboardContents(clipboardContents);
		
		//Paste the player's puzzle 
		if(pastedPuzzle != null) {
			board.clear(true);
			board.recordGivens();
			board.setPuzzle(pastedPuzzle);
			mainWindow.clearUndoableActions();
			mainWindow.setPuzzleVerified(false);
			mainWindow.puzzle.setSolved(false);
		}
		else {				
			JOptionPane.showMessageDialog(mainWindow.window, "Unsupported puzzle format.", "Paste", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Convert clipboard contents to simple puzzle format, return null if unknown format
	private int[] convertClipboardContents(final String clipboardContents) {			
		if(clipboardContents.length() != mainWindow.unit * mainWindow.unit) {
			return null;
		}
		
		final int[] puzzle = new int[mainWindow.unit * mainWindow.unit];
		
		for(int i = 0; i < clipboardContents.length(); ++i) {
			if(Character.isDigit(clipboardContents.charAt(i))) {					
				puzzle[i] = Integer.parseInt(clipboardContents.substring(i, i + 1));
			}
			else if(Constants.ZERO_DOT_FORMAT == clipboardContents.charAt(i)) {
				puzzle[i] = 0;
			}
			else {
				return null;
			}
		}
		return puzzle;
	}
	
	private String getClipboardContents() {
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable contents = clipboard.getContents(null);
		String result = null;
		
		final boolean isTextContent = contents != null &&
			contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			
		if(!isTextContent) {
			return result;
		}
		
		try {
			result = ((String)contents.getTransferData(DataFlavor.stringFlavor)).trim();
		}
		catch(UnsupportedFlavorException | IOException ex) {
			//Should never occur, as we check contents for stringFlavor before casting
			System.err.println("An exception occured while getting the clipboard contents");
			ex.printStackTrace();
		}
		
		return result;
	}
}

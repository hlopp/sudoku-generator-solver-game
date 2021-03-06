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

import com.matic.sudoku.Resources;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.io.FileFormatManager;
import com.matic.sudoku.io.PuzzleBean;
import com.matic.sudoku.io.UnsupportedPuzzleFormatException;

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
	public void actionPerformed(final ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		
		switch(actionCommand) {
		case MainWindow.COPY_STRING:
			handleCopyAction();
			break;
		case MainWindow.PASTE_STRING:
			handlePasteAction();
			break;	
		case MainWindow.CLEAR_COLORS_STRING:
			handleClearColorsAction();
			break;
		case MainWindow.CLEAR_PENCILMARKS_STRING:
			handleClearPencilmarksAction();
			break;
		}
	}
	
	private void handleClearPencilmarksAction() {
		final String message = Resources.getTranslation("pencilmarks.clear.message");
		final String title = Resources.getTranslation("pencilmarks.clear.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window,
				message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		board.clearPencilmarks(true);
		mainWindow.undoManager.undoPencilmarksEntries();
		mainWindow.updateUndoControls();
		mainWindow.clearPencilmarksMenuItem.setEnabled(false);
		mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
	}
	
	private void handleClearColorsAction() {
		final String message = Resources.getTranslation("colors.clear.message");
		final String title = Resources.getTranslation("colors.clear.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window,
				message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		board.clearColorSelections();
		mainWindow.undoManager.undoColorEntries();			
		mainWindow.updateUndoControls();
		mainWindow.clearColorsMenuItem.setEnabled(false);
		mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
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
		final String message = Resources.getTranslation("paste.message");
		final String title = Resources.getTranslation("paste.title");
		final int choice = JOptionPane.showConfirmDialog(mainWindow.window,
				message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		
		final FileFormatManager fileFormatManager = new FileFormatManager();
		PuzzleBean puzzleBean = null;
		try {
			puzzleBean = fileFormatManager.fromString(clipboardContents);
		} catch(final IOException e) {
			JOptionPane.showMessageDialog(mainWindow.window, 
					Resources.getTranslation("paste.invalid"), 
					Resources.getTranslation("paste.name"),
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch(final UnsupportedPuzzleFormatException e) {
			JOptionPane.showMessageDialog(mainWindow.window, e.getMessage(), 
					Resources.getTranslation("paste.name"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		mainWindow.symbolButtonActionHandler.userPencilmarks = null;
		mainWindow.gameMenuActionListener.updateBoard(puzzleBean);
		mainWindow.gameMenuActionListener.onPuzzleStateChanged(true);
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
		catch(final UnsupportedFlavorException | IOException ex) {
			//Should never occur, as we check contents for stringFlavor before casting
			System.err.println("An exception occured while getting the clipboard contents");
			ex.printStackTrace();
		}
		
		return result;
	}
}

package UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import nafcp.NAFCP;
import nafcp.Result;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSeparator;
import javax.swing.JButton;

public class FrameStart extends JFrame {

	private JPanel contentPane;
	private JTextField txtMinSup;
	private JTable table;
	private JTextField txtDatabase;
	private JTextField txtTransaction;
	private String[] columnName= {"STT","Products", "SUP"};
	private DefaultTableModel tableModel;
	private JButton btnNewButton;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameStart frame = new FrameStart();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrameStart() {
		setTitle("Chương trình");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0,0, 1375, 830);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblMinSup = new JLabel("MinSup (%): ");
		lblMinSup.setBounds(10, 70, 100, 24);
		contentPane.add(lblMinSup);
		
		txtMinSup = new JTextField();
		txtMinSup.setBounds(120, 70, 200, 25);
		contentPane.add(txtMinSup);
		txtMinSup.setColumns(10);		
		
		btnNewButton = new JButton("Start");
		btnNewButton.setBounds(350, 70, 100, 25);
		contentPane.add(btnNewButton);
		
		JLabel lblResult = new JLabel("Result: ");
		lblResult.setBounds(10, 117, 200, 25);
		contentPane.add(lblResult);
		
		JLabel lblDatabase = new JLabel("Database: ");
		lblDatabase.setBounds(10, 10, 100, 25);
		contentPane.add(lblDatabase);
		
		JLabel lblTransaction = new JLabel("Number of Transaction");
		lblTransaction.setBounds(350, 10, 140, 25);
		contentPane.add(lblTransaction);
		
		txtDatabase = new JTextField();
		txtDatabase.setColumns(10);
		txtDatabase.setBounds(120, 10, 200, 25);
		txtDatabase.setText("AdventureWorks2012");
		txtDatabase.disable();
		contentPane.add(txtDatabase);
		
		txtTransaction = new JTextField();
		txtTransaction.setColumns(10);
		txtTransaction.setBounds(500, 10, 200, 25);
		txtTransaction.setText("21208");
		txtTransaction.disable();
		contentPane.add(txtTransaction);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 153, 1347, 500);
		contentPane.add(scrollPane);
		
		table = new JTable(tableModel= new DefaultTableModel(
				new String[][] {
				},columnName) 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean[] columnEditables = new boolean[] {
					false, false, false, false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(60);
		table.getColumnModel().getColumn(2).setPreferredWidth(160);
		

		table.setRowHeight(22);
		table.setFont(new Font("Arial", Font.PLAIN, 15));
		scrollPane.setViewportView(table);
		
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				NAFCP alo = new NAFCP();
				try {
					float minSup = (float) (Integer.parseInt(txtMinSup.getText())*1.0);					
					List<Result> results = alo.runAlgorithm(minSup/100, "ouput.txt");
					tableModel.setRowCount(0);
					int i = 1;
					for (Result result : results) {
						String []rowdata = {i+"",result.getProduct(),result.getSup()};	
						i++;
						tableModel.addRow(rowdata);
					}
					table.setModel(tableModel);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
	}
	
}

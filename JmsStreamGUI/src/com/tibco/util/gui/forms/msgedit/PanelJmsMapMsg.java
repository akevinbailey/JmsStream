/*
 * Copyright (c) 2013.  TIBCO Software Inc.  ALL RIGHTS RESERVED.
 */

/*
 * Created by JFormDesigner on Tue Feb 03 22:53:26 CET 2009
 */

package com.tibco.util.gui.forms.msgedit;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tibco.util.jmshelper.MsgPropStruct;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Title:        <p>
 * Description:  <p>
 * @author A. Kevin Bailey
 * @version 2.5.0
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "FieldCanBeLocal", "CanBeFinal", "unchecked", "WeakerAccess"})
public class PanelJmsMapMsg extends JPanel {
    public PanelJmsMapMsg() {
        initComponents();
    }

    public ArrayList<MsgPropStruct> getMapMsg() {
        Vector vecData = ((DefaultTableModel)tblMapMsg.getModel()).getDataVector();
        ArrayList<MsgPropStruct> alMapMsg = new ArrayList<MsgPropStruct>();
        for (int i=0; i < vecData.size(); i++) {
            alMapMsg.add(new MsgPropStruct(((Vector)vecData.elementAt(i)).elementAt(0).toString()      // Name
                                            , ((Vector)vecData.elementAt(i)).elementAt(1).toString()    // Type
                                            , ((Vector)vecData.elementAt(i)).elementAt(2).toString())); // Value
        }

        return alMapMsg;
    }

    public void setMapMsg(ArrayList<MsgPropStruct> alMapMsg) {
        // Remove previous table data
        ((DefaultTableModel)tblMapMsg.getModel()).setRowCount(0);
        // Add fields to tblMapMsg table
        for (MsgPropStruct anAlMapMsg : alMapMsg) {
            Vector vecMsgProp = new Vector(3); // Number of columns
            vecMsgProp.add(anAlMapMsg.getName());   // Name
            vecMsgProp.add(anAlMapMsg.getType());   // Type
            vecMsgProp.add(anAlMapMsg.getValue());  // Value
            ((DefaultTableModel) tblMapMsg.getModel()).addRow(vecMsgProp);
        }
    }

    private void btnAddRowActionPerformed() {
        if (tblMapMsg.getSelectedRow() == -1) {
            _tmodMapMsg.addRow(new Vector());
        }
        else {
            _tmodMapMsg.insertRow(tblMapMsg.getSelectedRow() + 1, new Vector());
        }
    }

    private void btnRemoveRowActionPerformed() {
        if (tblMapMsg.getSelectedRow() > -1) {
            _tmodMapMsg.removeRow(tblMapMsg.getSelectedRow());
        }
    }

    private void btnUpActionPerformed() {
        int intIndex = tblMapMsg.getSelectedRow();
        if (intIndex > -1 && (intIndex - 1) > -1 ) {
            _tmodMapMsg.moveRow(intIndex, intIndex, intIndex - 1);
            tblMapMsg.setRowSelectionInterval(intIndex - 1, intIndex - 1);
        }
    }

    private void btnDownActionPerformed() {
        int intIndex = tblMapMsg.getSelectedRow();
        if (intIndex > -1 && (intIndex + 1) < tblMapMsg.getRowCount()) {
            _tmodMapMsg.moveRow(intIndex, intIndex, intIndex + 1);
            tblMapMsg.setRowSelectionInterval(intIndex + 1, intIndex + 1);
        }
    }

    private void tblMapMsgFocusLost(FocusEvent e) {
        // Add focus listener to the table cells in order to stop editing when the cell looses focus
        JTable theTable = (JTable)e.getSource();
        Component comp = theTable.getEditorComponent();
        if (comp != null) {
            boolean alreadySet = false;
            FocusListener listeners[] = comp.getFocusListeners();
            for (int i = 0, m = listeners.length; i < m; i++) {
                FocusListener l = listeners[i];
                if (l instanceof FocusAdapter) {
                    alreadySet = true;
                    break;
                }
            }
            if (!alreadySet) {
                comp.addFocusListener(
                        new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                TableCellEditor ed = tblMapMsg.getCellEditor();
                                if (ed != null) {
                                    ed.stopCellEditing();
                                }
                            }
                        }
                );
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblMapMsgPanel = new JLabel();
        scrMapMsg = new JScrollPane();
        tblMapMsg = new JTable();
        pnlTableButtons = new JPanel();
        btnAddRow = new JButton();
        btnRemoveRow = new JButton();
        btnUp = new JButton();
        btnDown = new JButton();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "2dlu, default, 2dlu, default:grow, $lcgap, 2dlu",
            "2dlu, $lgap, default, $ugap, default, $lgap, fill:default:grow, $lgap, 2dlu"));

        //---- lblMapMsgPanel ----
        lblMapMsgPanel.setText("Map Message Type");
        lblMapMsgPanel.setFont(new Font("Tahoma", Font.BOLD, 11));
        add(lblMapMsgPanel, cc.xywh(2, 3, 3, 1));

        //======== scrMapMsg ========
        {

            //---- tblMapMsg ----
            tblMapMsg.setCellSelectionEnabled(true);
            tblMapMsg.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblMapMsg.setModel(new DefaultTableModel(
                new Object[][] {
                    {null, null, null},
                },
                new String[] {
                    "Name", "Type", "Value"
                }
            ) {
                Class[] columnTypes = new Class[] {
                    String.class, String.class, String.class
                };
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnTypes[columnIndex];
                }
            });
            {
                TableColumnModel cm = tblMapMsg.getColumnModel();
                cm.getColumn(1).setCellEditor(new DefaultCellEditor(
                    new JComboBox(new DefaultComboBoxModel(new String[] {
                        "Boolean",
                        "Character",
                        "Double",
                        "Float",
                        "Integer",
                        "Long",
                        "Number",
                        "String"
                    }))));
            }
            tblMapMsg.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tblMapMsg.setSurrendersFocusOnKeystroke(true);
            tblMapMsg.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    tblMapMsgFocusLost(e);
                }
            });
            scrMapMsg.setViewportView(tblMapMsg);
        }
        add(scrMapMsg, cc.xy(4, 5));

        //======== pnlTableButtons ========
        {
            pnlTableButtons.setLayout(new FormLayout(
                "14dlu",
                "10dlu, bottom:14dlu, top:14dlu, $ugap, bottom:14dlu, top:14dlu"));

            //---- btnAddRow ----
            btnAddRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordAdd1.png")));
            btnAddRow.setFont(UIManager.getFont("Button.font"));
            btnAddRow.setFocusPainted(false);
            btnAddRow.setMargin(new Insets(3, 13, 3, 14));
            btnAddRow.setMaximumSize(new Dimension(41, 41));
            btnAddRow.setMinimumSize(new Dimension(41, 41));
            btnAddRow.setPreferredSize(new Dimension(41, 41));
            btnAddRow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnAddRowActionPerformed();
                }
            });
            pnlTableButtons.add(btnAddRow, cc.xy(1, 2));

            //---- btnRemoveRow ----
            btnRemoveRow.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/recordDelete1.png")));
            btnRemoveRow.setFocusPainted(false);
            btnRemoveRow.setFont(UIManager.getFont("Button.font"));
            btnRemoveRow.setMargin(new Insets(3, 12, 3, 15));
            btnRemoveRow.setMaximumSize(new Dimension(41, 41));
            btnRemoveRow.setMinimumSize(new Dimension(41, 41));
            btnRemoveRow.setPreferredSize(new Dimension(41, 41));
            btnRemoveRow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnRemoveRowActionPerformed();
                }
            });
            pnlTableButtons.add(btnRemoveRow, cc.xy(1, 3));

            //---- btnUp ----
            btnUp.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/moveUp1.png")));
            btnUp.setFocusPainted(false);
            btnUp.setMargin(new Insets(3, 13, 3, 14));
            btnUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnUpActionPerformed();
                }
            });
            pnlTableButtons.add(btnUp, cc.xy(1, 5));

            //---- btnDown ----
            btnDown.setFocusPainted(false);
            btnDown.setIcon(new ImageIcon(getClass().getResource("/com/tibco/util/gui/resources/moveDown1.png")));
            btnDown.setMargin(new Insets(3, 13, 3, 14));
            btnDown.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnDownActionPerformed();
                }
            });
            pnlTableButtons.add(btnDown, cc.xy(1, 6));
        }
        add(pnlTableButtons, cc.xywh(2, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        // Get the DefaultTableModel from tblUserProperties
        _tmodMapMsg = (DefaultTableModel)(tblMapMsg.getModel());
    }

    private DefaultTableModel _tmodMapMsg;

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblMapMsgPanel;
    private JScrollPane scrMapMsg;
    private JTable tblMapMsg;
    private JPanel pnlTableButtons;
    private JButton btnAddRow;
    private JButton btnRemoveRow;
    private JButton btnUp;
    private JButton btnDown;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}

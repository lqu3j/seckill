package com.github.lyrric.ui;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.TableModel;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.service.SecKillService;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class MainFrame extends JFrame {

    SecKillService service = new SecKillService();
    /**
     * 疫苗列表
     */
    private List<VaccineList> vaccines;

    JButton startBtn;

    JButton setCookieBtn;

    JButton setMemberBtn;

    JTable vaccinesTable;

    JButton refreshBtn;

    DefaultTableModel tableModel;

    JTextArea note;
    public MainFrame() {
        setLayout(null);
        setTitle("Just For Fun");
        setBounds(500 , 500, 680, 340);
        init();
        setLocationRelativeTo(null);
        setVisible(true);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(){
        startBtn = new JButton("开始");
        startBtn.addActionListener(e -> start());

        setCookieBtn = new JButton("设置Cookie");
        setCookieBtn.addActionListener((e)->{
            ConfigDialog dialog = new ConfigDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if(dialog.success()){
                appendMsg("设置cookie成功");
            }

        });

        setMemberBtn = new JButton("选择成员");
        setMemberBtn.addActionListener((e)->{
            MemberDialog dialog = new MemberDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if(dialog.success()){
                appendMsg("已设置成员");
            }
        });

        refreshBtn = new JButton("刷新疫苗列表");
        refreshBtn.addActionListener((e)->{
            refreshVaccines();
        });

        note = new JTextArea();
        note.append("日记记录：\r\n");
        note.setEditable(false);
        note.setAutoscrolls(true);
        note.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(note);
        scroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        String[] columnNames = { "id", "疫苗名称","医院名称","秒杀时间" };
        tableModel = new TableModel(new String[0][], columnNames);
        vaccinesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(vaccinesTable);

        scrollPane.setBounds(10,10,460,200);

        startBtn.setBounds(370, 230, 100, 30);

        setCookieBtn.setBounds(20, 230, 100, 30);
        setMemberBtn.setBounds(130, 230, 100, 30);
        refreshBtn.setBounds(240, 230,120, 30);

        scroll.setBounds(480, 10, 180, 280);

        add(scrollPane);
        add(scroll);
        add(startBtn);
        add(setCookieBtn);
        add(setMemberBtn);
        add(refreshBtn);
    }



    private void refreshVaccines(){
        try {
            vaccines = service.getVaccines();
            //清除表格数据
            //通知模型更新
            ((DefaultTableModel)vaccinesTable.getModel()).getDataVector().clear();
            ((DefaultTableModel)vaccinesTable.getModel()).fireTableDataChanged();
            vaccinesTable.updateUI();//刷新表
            if(vaccines != null && !vaccines.isEmpty()){
                for (VaccineList t : vaccines) {
                    String[] item = { t.getId().toString(), t.getVaccineName(),t.getName() ,t.getStartTime()};
                    tableModel.addRow(item);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            appendMsg("未知错误");
        } catch (BusinessException e) {
            appendMsg("错误："+e.getErrMsg()+"，errCode"+e.getCode());
        }
    }
    private void start(){
        if(StringUtils.isEmpty(Config.cookies)){
            appendMsg("请配置cookie!!!");
            return ;
        }
        if(vaccinesTable.getSelectedRow() < 0){
            appendMsg("请选择要抢购的疫苗");
            return ;
        }

        int selectedRow = vaccinesTable.getSelectedRow();
        Integer id = vaccines.get(selectedRow).getId();
        String startTime = vaccines.get(selectedRow).getStartTime();
        new Thread(()->{
            try {
                service.startSecKill(id, startTime, this);
            } catch (ParseException | InterruptedException e) {
                appendMsg("解析开始时间失败");
                e.printStackTrace();
            }
        }).start();

    }


    public void appendMsg(String message){
        note.append(message);
        note.append("\r\n");
    }
}

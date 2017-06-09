package jmanager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import subpack.*;

public class FilePane {

    //Панели с контентом
    private JPanel contentPanel=new JPanel(new BorderLayout());
    private JPanel northPanel=new JPanel();
    private JPanel centerPanel=new JPanel(new BorderLayout());
    private JPanel southPanel=new JPanel();

    //Компонент для отображения списка быстрого доступа к дискам
    private JComboBox<String> diskList;

    //Кнопка "Вверх"
    private final JButton upButton;

    //Кнопка "Обновить"
    private final JButton refreshButton;

    //Таблица для отображения содержимого папки и модель данных для нее
    private JTable tab;
    private TabModel tm;
    private TabCellRenderer tcr;
    private TabHeaderRenderer thc;

    //Метка для отображения дополнительной информации
    JTextField fullFolderPath=new JTextField();

    //Текущая папка, содержимое которой отображается в tab
    private File folder=new File(System.getProperty("user.home"));
    private boolean hiddenEnabled=false;

    //Чек бокс для выбора отображения скрытых файлов и папок
    private JCheckBox showHiddenElements=new JCheckBox("Показывать скрытые элементы", hiddenEnabled);

    //Всплывающее меню
    private JPopupMenu pm=null;
    private String pm_actionPref;

    public FilePane(int W, int H) {
        //Создаем панель контента
        contentPanel.setPreferredSize(new Dimension(W, H));

        //Создаем северную панель
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        upButton=new JButton(new ImageIcon("icons\\up_blue.png"));
        upButton.setToolTipText("Вверх");
        northPanel.add(upButton);
        northPanel.add(Box.createHorizontalStrut(10));
        northPanel.add(new JLabel("Диск:"));
        northPanel.add(Box.createHorizontalStrut(5));
        diskList=new JComboBox<>();
        diskList.setMaximumSize(new Dimension(30, 24));
        diskList.setToolTipText("Сменить диск");
        refreshDiskList();
        northPanel.add(diskList);
        northPanel.add(Box.createHorizontalGlue());
        refreshButton=new JButton(new ImageIcon("icons\\refresh_blue.png"));
        refreshButton.setToolTipText("Обновить");
        northPanel.add(refreshButton);
        contentPanel.add(northPanel, BorderLayout.NORTH);

        //Создаем центральную панель с таблицей, представляющей текущий каталог
        tm=new TabModel(folder, hiddenEnabled);
        tcr=new TabCellRenderer();
        thc=new TabHeaderRenderer();
        tab=new JTable(tm);
        tab.setDefaultRenderer(Object.class, tcr);
        tab.getTableHeader().setDefaultRenderer(thc);
        tab.setRowHeight(19);
        tab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane sp=new JScrollPane(tab);
        tab.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tab.setShowVerticalLines(false);
        tab.setGridColor(new Color(220,220,200));
        tab.getTableHeader().setReorderingAllowed(false);
        tab.getTableHeader().setFont(new Font(null, Font.BOLD, 12));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        centerPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        //Устанавливаем ширину столбцов в зависимости от ширины содержащего таблитцу контейнера
        tab.getColumnModel().getColumn(0).setPreferredWidth((int)(contentPanel.getPreferredSize().getWidth()*0.52));
        tab.getColumnModel().getColumn(1).setPreferredWidth((int)(contentPanel.getPreferredSize().getWidth()*0.1));
        tab.getColumnModel().getColumn(2).setPreferredWidth((int)(contentPanel.getPreferredSize().getWidth()*0.1));
        tab.getColumnModel().getColumn(3).setPreferredWidth((int)(contentPanel.getPreferredSize().getWidth()*0.13));
        tab.getColumnModel().getColumn(4).setPreferredWidth((int)(contentPanel.getPreferredSize().getWidth()*0.13));

        //Создаем южную панель
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5));
        fullFolderPath.setEditable(false);
        fullFolderPath.setText(folder.getAbsolutePath());
        southPanel.add(fullFolderPath);
        southPanel.add(showHiddenElements);
        showHiddenElements.setHorizontalTextPosition(SwingConstants.LEFT);
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        //Создаем обработчики событий
        //Обработчик кнопки "Вверх"
        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File nextFolder=folder.getParentFile();
                if(nextFolder==null)nextFolder=folder.toPath().getRoot().toFile();
                folder=nextFolder;
                fullFolderPath.setText(folder.getAbsolutePath());
                tm.refresh(folder, hiddenEnabled);
            }
        });

        //Обработчик смены текущего диска
        diskList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(diskList.getSelectedItem()==null)return;
                File selDisk=new File(diskList.getSelectedItem().toString());
                if(selDisk.exists() & selDisk.canRead()){
                    folder=selDisk;
                    fullFolderPath.setText(folder.getAbsolutePath());
                    tm.refresh(folder, hiddenEnabled);
                    return;
                }
                JOptionPane.showMessageDialog(contentPanel, "Диск "+diskList.getSelectedItem().toString()+" не доступен для чтения.", "Ошибка доступа к диску", JOptionPane.ERROR_MESSAGE);
                refreshDiskList();
            }
        });

        //Обработчик кнопки "Обновить"
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDiskList();
                tm.refresh(folder, hiddenEnabled);
                fullFolderPath.setText(folder.getAbsolutePath());
            }
        });

        //Обработчик выбора режима отображения скрытых элементов
        showHiddenElements.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                hiddenEnabled=!hiddenEnabled;
                tm.refresh(folder, hiddenEnabled);
            }
        });

        //Обработчик щелчка на столбце
        tab.getTableHeader().addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getButton()!=1)return;
                if(e.getClickCount()!=1)return;
                tm.sort(tab.columnAtPoint(new Point(e.getX(), e.getY())));

                //Вызов этого метода необходим для корректной прорисовки заголовков столбцов
                tab.getTableHeader().repaint();
            }
        });

        //Обработчик мышки на таблице
        tab.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getButton()!=1)return;
                if(e.getClickCount()!=2)return;
                File f=(File)tm.getValueAt(tab.getSelectedRow(), 0);

                //Проверяем существование и доступность выбранного элемента
                if(!f.exists() | !f.canRead()){
                    JOptionPane.showMessageDialog(contentPanel, f.getName()+" не существует или недоступен для чтения", "Нет доступа к элементу", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                //Пытаемся открыть элемент
                //Если это папка
                if(f.isDirectory()){
                    folder=f;
                    tm.refresh(folder, hiddenEnabled);
                    fullFolderPath.setText(folder.getAbsolutePath());
                    return;
                }

                //Если это файл
                if(f.isFile()){
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(contentPanel, "Не удалось открыть файл "+f.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e){
                if(e.getButton()!=3)return;
                if(pm==null)return;
                JMenuItem item;
                String itemActionCom;
                for(int i=0;i<pm.getComponentCount();i++){
                    if(!(pm.getComponent(i) instanceof JMenuItem))continue;
                    item=(JMenuItem)pm.getComponent(i);
                    itemActionCom=item.getActionCommand();
                    if(itemActionCom.indexOf('_')>(-1)){
                        itemActionCom=itemActionCom.substring(0, itemActionCom.indexOf('_')+1)+pm_actionPref;
                    }
                    if(itemActionCom.equals("copy_left"))item.setText("Копировать вправо");
                    if(itemActionCom.equals("copy_right"))item.setText("Копировать влево");
                    if(itemActionCom.equals("move_left"))item.setText("Переместить вправо");
                    if(itemActionCom.equals("move_right"))item.setText("Переместить влево");
                    item.setActionCommand(itemActionCom);
                }
                pm.show(tab, e.getX(), e.getY());
            }

        });

    }

    //Метод обновляет список доступных дисков
    private void refreshDiskList(){
        File[] roots=File.listRoots();
        DefaultComboBoxModel<String> cbm=new DefaultComboBoxModel<>();
        for(File f: roots)if(f.exists() & f.canRead())cbm.addElement(f.toString());
        for (File root : roots) {
            if (root.toString().equals(folder.toPath().getRoot().toString())) {
                cbm.setSelectedItem(root);
                break;
            }
        }
        diskList.setModel(cbm);
    }

    //Метод возвращает панель с контентом
    public JPanel getContentPanel(){
        return contentPanel;
    }

    //Метод возвращает текущую папку
    public File getFolder(){
        return folder;
    }

    //Метод возвращает массив выбранных файлов и папок
    public File[] getSelectedItems(){
        int index[]=tab.getSelectedRows();
        File[] f=new File[index.length];
        for(int j=0;j<index.length;j++)f[j]=(File)tm.getValueAt(index[j], 0);
        return f;
    }

    //Метод обновляет содержимое панели и список дисков
    public void refreshPane(){
        refreshDiskList();
        tm.refresh(folder, hiddenEnabled);
        fullFolderPath.setText(folder.getAbsolutePath());
    }

    //Метод устанавливает всплывающе меню для панели
    public void setPopupMenu(JPopupMenu p, String actionPref){
        pm=p;
        pm_actionPref=actionPref;
    }

    //Метод выделяет все строки таблицы
    public void selectAll(){
        tab.getSelectionModel().setSelectionInterval(0, tm.getRowCount());
    }

    //Метод делает выбранными строки, соответствующие файлам (папкам) из массива f
    public void setSelected(File[] f){
        tab.getSelectionModel().clearSelection();
        for(File fFile: f){
            for(int i=0;i<tm.getRowCount();i++)if(fFile.equals(tm.getValueAt(i, 0)))tab.getSelectionModel().addSelectionInterval(i, i);
        }
    }

    //Метод делает выбранной строку, соответствующую файлу (папке) f
    public void setSelected(File f){
        tab.getSelectionModel().clearSelection();
        for(int i=0;i<tm.getRowCount();i++)if(f.equals(tm.getValueAt(i, 0))){
            tab.getSelectionModel().setSelectionInterval(i, i);
            break;
        }
    }

    @Override
    public String toString(){
        return pm_actionPref;
    }

}

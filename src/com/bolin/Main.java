package com.bolin;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bolin.utils.ConfigFileUtil;

/**
 * @author bolin
 * @create 2017年4月20日
 *
 */
public class Main {

	private JFrame frame;
	private JButton choseProjectPathBtn;
	private JTextField projectPath;
	private JLabel label;
	private JTextField configPath;
	private JButton startReplaceBtn;
	private JTextArea logArea;

	private final String[] removedJars = new String[]{"jsp-api", "servlet"};
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 559, 409);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		projectPath = new JTextField();
		projectPath.setBounds(116, 10, 286, 23);
		frame.getContentPane().add(projectPath);
		projectPath.setColumns(10);
		
		choseProjectPathBtn = new JButton("浏览");
		choseProjectPathBtn.setBounds(412, 10, 93, 23);
		choseProjectPathBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser addChooser = new JFileChooser("E:\\deploy");
				addChooser.setDialogTitle("选择项目文件目录");
				addChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnval = addChooser.showOpenDialog(frame);
                if(returnval == JFileChooser.APPROVE_OPTION) 
                { 
                    File file = addChooser.getSelectedFile();
                    String str = file.getPath(); 
                    projectPath.setText(str);
                } 
				
			}
		});
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(choseProjectPathBtn);
		
		
		JLabel lblNewLabel = new JLabel("项目文件目录");
		lblNewLabel.setBounds(23, 10, 83, 23);
		frame.getContentPane().add(lblNewLabel);
		
		label = new JLabel("配置文件目录");
		label.setBounds(23, 43, 83, 23);
		frame.getContentPane().add(label);
		
		configPath = new JTextField();
		configPath.setColumns(10);
		configPath.setBounds(116, 43, 286, 23);
		frame.getContentPane().add(configPath);
		
		JButton choseConfigFilePathBtn = new JButton("浏览");
		choseConfigFilePathBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser addChooser = new JFileChooser("E:\\deploy\\配置文件");
				addChooser.setDialogTitle("选择配置文件目录");
				addChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnval=addChooser.showOpenDialog(frame);
                if(returnval==JFileChooser.APPROVE_OPTION) 
                { 
                    File file = addChooser.getSelectedFile();
                    String str = file.getPath(); 
                    configPath.setText(str);
                } 
			}
		});
		choseConfigFilePathBtn.setBounds(412, 43, 93, 23);
		frame.getContentPane().add(choseConfigFilePathBtn);
		
		startReplaceBtn = new JButton("替换配置文件");
		startReplaceBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String projectFilePath = projectPath.getText();
				String configFilePath = configPath.getText();
				
				logArea.setText("");
				if(StringUtils.isBlank(projectFilePath)){
					logArea.append(now() + "未选择项目文件目录！\n");
					return;
				}
				if(StringUtils.isBlank(configFilePath)){
					logArea.append(now() + "未选择配置文件目录！\n");
					return;
				}
				
				logArea.append(now() + "开始替换配置文件...\n");
				File configFileDir = new File(configFilePath);
				File[] configFiles = configFileDir.listFiles();
				if(configFiles == null || configFiles.length < 1){
					logArea.append(now() + "选择的配置文件目录中没有可以替换的配置文件！\n");
					return;
				}else{
					
					for ( File configFile : configFiles ) {
						logArea.append("---------------------------------------------------------\n");
						logArea.append(now() + "配置文件:" + configFile.getName() + "\n");
						
						ConfigFileUtil util = new ConfigFileUtil();
						String findResultStr = util.find(projectFilePath, configFile.getName());
						JSONObject findResult = JSON.parseObject(findResultStr);
						if(!findResult.getBooleanValue("success")){
							logArea.append(now() + findResult.getString("msg") + "\n");
							return;
						}
						String targetFileDir =  findResult.getString("msg");
						if(util.isXmlFile(targetFileDir)){
							util.replaceXml(configFile.getAbsolutePath(), targetFileDir);
						}else if(util.isPropertyFile(targetFileDir)){
							util.replaceProperties(configFile.getAbsolutePath(), targetFileDir);
						}else{
							logArea.append(now() + "未知的配置文件类型：不处理\n");
						}
					}
					logArea.append(now() + "替换配置文件结束！\n");
				}
			}
		});
		startReplaceBtn.setBounds(23, 86, 128, 23);
		frame.getContentPane().add(startReplaceBtn);
		
		logArea = new JTextArea();
		logArea.setVisible(true);
        JScrollPane jsp = new JScrollPane(logArea);
        jsp.setVisible(true);
        jsp.setBounds(23, 115, 482, 246);
        frame.getContentPane().add(jsp, BorderLayout.CENTER);
        
        JButton removeJarBtn = new JButton("删除jar包");
        removeJarBtn.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {
        		String projectFilePath = projectPath.getText();
				logArea.setText("");
				if(StringUtils.isBlank(projectFilePath)){
					logArea.append(now() + "未选择项目文件目录！\n");
					return;
				}
				logArea.append(now() + "开始删除jar文件...\n");
				String jarDir = projectFilePath + File.separator + "WEB-INF" + File.separator + "lib";
				File jarFileDir = new File(jarDir);
				
				File[] jars = jarFileDir.listFiles();
				if(jars.length > 0){
					for ( File jar : jars ) {
						if(StringUtils.contains(jar.getName(), "jsp-api") || 
						   StringUtils.contains(jar.getName(), "servlet-api")){
							logArea.append(now() + "删除jar：" + jar.getName());
							jar.delete();
						}
					}
				}else{
					logArea.append("无需要删除的jar文件！");
				}
        	}
        });
        removeJarBtn.setBounds(158, 86, 105, 23);
        frame.getContentPane().add(removeJarBtn);
        frame.setVisible(true);
	}
	
	private String now(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ";
	}
}

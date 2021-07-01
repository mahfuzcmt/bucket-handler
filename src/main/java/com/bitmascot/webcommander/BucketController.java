package com.bitmascot.webcommander;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

@RestController
public class BucketController {

	private final String systemToken = "Dedfdfadf64543543656546Sfsdfasdfdsafa5453245345435325";

	public static void exeCuteCommand(String command) {
		try {
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			ProcessBuilder builder = new ProcessBuilder();
			if (isWindows) {
				builder.command("cmd.exe", "/c", command);
			} else {
				builder.command("sh", "-c", command);
			}
			Process process = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
				System.out.println("Cmd Response: " + line);
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/calls3bucket")
	public String moveFilesToS3Bucket(@RequestParam Map<String,String> requestParams) {
		try {
			if (systemToken.equals(requestParams.get("token"))) {
				InputStream inputStream = new FileInputStream(new File("binary-repo/4.0.0/build-log.yml"));
				InputStream configIs = new FileInputStream(new File("config.yml"));
				Yaml yaml = new Yaml();
				Map<String, Object> data = yaml.load(inputStream);
				Map<String, Object> configData = yaml.load(configIs);

				String mode = requestParams.get("mode");
				String bucketVersion = mode + "/" + data.get("bucketVersion").toString();
				String appVersion = data.get("appVersion").toString();

				//extract war and keep a safe location
				String sourceLocation = configData.get("outputLocation").toString();
				String bucketName = configData.get("bucketName").toString();

				if (configData.get("cleanOutputLoc").equals(true)) {
					System.out.println("========Cleaning output location of client===============");
					String command = "rm -rf " + sourceLocation + "/*";
					System.out.println("command: "+command);
					exeCuteCommand(command); //clear before start
				}

				if (configData.get("copyWar").equals(true)) {
					System.out.println("========Coping war to output location===============");
					String command = "cp " + configData.get("sourceFileLoc").toString() + "/" + appVersion + "/" + configData.get("sourceFileName").toString() + " " + configData.get("outputLocation").toString() + "";
					System.out.println("command: "+command);
					exeCuteCommand(command);
					System.out.println("========War Copied!===============");
				}

				if (configData.get("extractWar").equals(true)) {
					System.out.println("========Going to extract War===============");
					System.out.println("========Source Location " + configData.get("sourceFileLoc").toString() + "/" + appVersion + "/" + configData.get("sourceFileName").toString() + "===============");
					System.out.println("========Output Location " + configData.get("outputLocation").toString() + "===============");
					String command = "cd " + configData.get("outputLocation").toString() + " && jar xvf " + configData.get("sourceFileName").toString() + "";
					System.out.println("command: "+command);
					exeCuteCommand(command);
					System.out.println("========Extract Done!===============");
				}


				//Clean s3 target directory file to AWS
				if (configData.get("cleanS3").equals(true)) {
					System.out.println("========Going to clean s3 bucket!===============");
					String command = "aws s3 rm s3://" + bucketName + "/" + bucketVersion + " --recursive";
					System.out.println("command: "+command);
					exeCuteCommand(command);
				}
				//Copy file to AWS
				if (configData.get("copyToS3").equals(true)) {
					System.out.println("========Coping resources to AWS S3===============");
					String command = "aws s3 cp " + sourceLocation + "/wc/" + appVersion + " s3://" + bucketName + "/" + bucketVersion + "/wc/" + appVersion + "/ --recursive --acl public-read";
					System.out.println("command: "+command);
					exeCuteCommand(command);
					System.out.println("========Copied!===============");
				}
				if (configData.get("showS3Dir").equals(true)) {
					System.out.println("========Below are the file structure===============");
					String command = "aws s3 ls s3://webcommander-dev/" + bucketVersion + "/wc/" + appVersion+"/";
					System.out.println("command: "+command);
					exeCuteCommand(command);
					System.out.println("========Done!===============");
				}
				return "Success!";
			} else {
				return "Sorry, You're not authorized!";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return e.getMessage();
		}
	}
}

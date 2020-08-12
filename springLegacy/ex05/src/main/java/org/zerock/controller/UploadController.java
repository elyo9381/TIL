package org.zerock.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.domain.AttachFileDTO;

import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

@Controller
@Log4j
public class UploadController {
	
	
	private String getFolder() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String str = sdf.format(date);
		return str.replace("-", File.separator);
	}
	
	

	private boolean checkImageType(File file) {
		
		
		log.info("checkImageType.name : " + file.getName());
		 Magic magic = new Magic();
		 MagicMatch match;
		try {
			
//			Magic magic = new Magic();
//            MagicMatch match = magic.getMagicMatch(file, false);
//            String contentType = match.getMimeType();
//            return contentType.startsWith("image");
			match = magic.getMagicMatch(file, false);
			log.info("checkImageType.try : " + match.getMimeType());
			if(match.getMimeType().contains("image")) {
				return true;
			}
			else {
				return false;
			}
		} catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	
	@GetMapping("/uploadForm")
	public void uploadForm() {
		log.info("upload form");
	}
	
	@GetMapping("/uploadAjax")
	public void uploadAjax() {
		log.info("upload ajax");
	}
	
	@GetMapping("display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName){
		log.info("file name : " + fileName);
		
		File file = new File("/Users/eyw/springUpload/" + fileName);
		
		log.info("file : " + file);
		
		ResponseEntity<byte[]> result = null;
		
		try {
			HttpHeaders header = new HttpHeaders();
			
			header.add("Content-Type", Files.probeContentType(file.toPath()));
			result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),
					header, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@PostMapping("/uploadFormAction")
	public void uploadFormPost(MultipartFile[] uploadFile, Model model) {
		
		String uploadFolder = "/Users/eyw/springUpload";
		
		
		for(MultipartFile multipartFile : uploadFile) {
			log.info("--------------------------------");
			log.info("Upload File Name : " + multipartFile.getOriginalFilename());
			log.info("Upload file size ; " + multipartFile.getSize());
			
			log.info("upload File size: " + multipartFile.getSize());
			
			File saveFile = new File(uploadFolder, multipartFile.getOriginalFilename());
			
			try {
				multipartFile.transferTo(saveFile);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
	
	@PostMapping(value = "/uploadAjaxAction", produces =  MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	 public ResponseEntity<List<AttachFileDTO> > 
	uploadAjaxPost(MultipartFile[] uploadFile) {
		
	List<AttachFileDTO> list = new ArrayList<>();

	String uploadFolder = "/Users/eyw/springUpload";
	 

	// make folder --------
	String uploadFolderPath = getFolder(); 
	 File uploadPath = new File(uploadFolder, uploadFolderPath);
	 log.info("upload path: " + uploadPath);

	 if (uploadPath.exists() == false) {
	 uploadPath.mkdirs();
	 }
	 // make yyyy/MM/dd folder

	 for (MultipartFile multipartFile : uploadFile) {
		 AttachFileDTO attachFileDTO = new AttachFileDTO();
		 String uploadFileName = multipartFile.getOriginalFilename();

	 // IE has file path
	 uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("/") +
	 1);
	 log.info("only file name: " + uploadFileName);
	 attachFileDTO.setFileName(uploadFileName);
	 UUID uuid = UUID.randomUUID();

	 uploadFileName = uuid.toString() + "_" + uploadFileName;

	 try {
		 File saveFile = new File(uploadPath, uploadFileName);
		 multipartFile.transferTo(saveFile);
		 
		 attachFileDTO.setUuid(uuid.toString());
		 attachFileDTO.setUploadPath(uploadFolderPath);
		 
	 // check image type file
		 if (checkImageType(saveFile)) {
	
		 attachFileDTO.setImage(true);
		 
		 FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath, "s_" +
		 uploadFileName));
	
		 Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100,
		 100);
	
		 thumbnail.close();
		 }
		 // add to list
		 list.add(attachFileDTO);

	 } catch (Exception e) {
	 log.info(" save file :" + e.getMessage());
	 e.printStackTrace();
	 } //end catch
	 } // end for

	 return new ResponseEntity<>(list,HttpStatus.OK);
	 }
	
	
	 @GetMapping(value = "/download", produces =
	 MediaType.APPLICATION_OCTET_STREAM_VALUE)
	 @ResponseBody
	 public ResponseEntity<org.springframework.core.io.Resource> 
	 	downloadFile(@RequestHeader("User-Agent") String userAgent,
	 			String fileName) throws UnsupportedEncodingException {

     org.springframework.core.io.Resource resource = new FileSystemResource("/Users/eyw/springUpload/" + fileName);
	 
     if(resource.exists() == false) {
    	 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
     }
     
     String resourceName = resource.getFilename();
     String resourceOriginalNameString = resourceName.substring(resourceName
    		 .indexOf("_")+1);
     HttpHeaders headers = new HttpHeaders();
     
     String downName = null;
     if(userAgent.contains("Trident")) {
    	 log.info("IE browser");
    	 downName = URLEncoder.encode(resourceOriginalNameString, "UTF-8");
    	 log.info("Edge name : " + downName);
     } else  if (userAgent.contains("Edge")) {
	 	log.info("Edge browser");
	 	downName = URLEncoder.encode(resourceOriginalNameString, "UTF-8");
	 } else {
    	 log.info("chrome browser");
    	 downName = new String(resourceOriginalNameString.getBytes("UTF-8"),"ISO-8859-1");
     }
	 	
     headers.add("Content-Disposition",
			 "attachment; filename="+ downName);
	 
	 return new ResponseEntity<Resource>(resource, headers,HttpStatus.OK);
	 }	
	 
	 @PostMapping("/deleteFile")
		@ResponseBody
		public ResponseEntity<String> deleteFile(String fileName, String type) {

			log.info("deleteFile: " + fileName);

			File file;

			try {
				file = new File("/Users/eyw/springUpload/" + URLDecoder.decode(fileName, "UTF-8"));

				file.delete();

				if (type.equals("image")) {

					String largeFileName = file.getAbsolutePath().replace("s_", "");

					log.info("largeFileName: " + largeFileName);

					file = new File(largeFileName);

					file.delete();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

			return new ResponseEntity<String>("deleted", HttpStatus.OK);

		}
}

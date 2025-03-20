package cn.czh.utils;

import org.springframework.http.MediaType;

/**
 * 文件类型的工具类
 */
public class FileTypeUtil {

	/**
	 * 图片类型
	 */
	public final static String[] IMG_FILE_TYPE = { "jpg", "bmp", "png", "gif" ,"jpeg"};

	/**
	 * 视频类型
	 */
	public final static String[] VIDEO_FILE_TYPE = { "mp4", "gmf", "wmv", "avi" };

	/**
	 * 日志文件
	 */
	public final static String[] LOG_FILE_TYPE = { "log", "txt", "doc", "docx" };
	
	/**
	 * 音频文件
	 */
	public final static String[] AUDIO_FILE_TYPE = { "wav", "mp3" };

	/**
	 * 图片文件
	 */
	public final static int IMG_FILE = 1;
	/**
	 * 视频文件
	 */
	public final static int VIDEO_FILE = 2;

	/**
	 * 日志文件
	 */
	public final static int LOG_FILE = 3;
	
	/**
	 * 音频文件
	 */
	public final static int AUDIO_FILE = 4;

	/**
	 * 通过文件类型判断文件是否符合所属类型
	 * @param fileType 文件类型 为FileTypeUtil中的 IMG_FILE ,VIDEO_FILE,VIDEO_FILE
	 */
	public static String isFileTypeExists(int fileType, String fileName) {
		switch (fileType) {
		case LOG_FILE:
			if (bLogFileType(fileName)) {
				return null;
			} else {
				return "日志类型不正确!";
			}
		case IMG_FILE:
			if (bImgFileType(fileName)) {
				return null;
			} else {
				return "图片类型不正确!";
			}
		case VIDEO_FILE:
			if (bVideoFileType(fileName)) {
				return null;
			} else {
				return "视频类型不正确!";
			}
		case AUDIO_FILE:
			if (bAudioFileType(fileName)) {
				return null;
			} else {
				return "视频类型不正确!";
			}
		default:
			return "没有指定文件类型";
		}

	}

	/**
	 * 检查是否日志文件
	 */
	public static boolean bLogFileType(String fileName) {
		String fileSuffix = getFileSuffix(fileName);
        for (String s : LOG_FILE_TYPE) {
            if (s.equals(fileSuffix)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * 检查是否音频文件
	 */
	public static boolean bAudioFileType(String fileName) {
		String fileSuffix = getFileSuffix(fileName);
        for (String s : AUDIO_FILE_TYPE) {
            if (s.equals(fileSuffix)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * 判断文件名是否为图片类型
	 */
	public static boolean bImgFileType(String fileName) {
		String fileSuffix = getFileSuffix(fileName);
        return isImgType(fileSuffix);
	}

	/**
	 * 视频格式
	 */
	public static boolean bVideoFileType(String fileName) {
		String fileSuffix = getFileSuffix(fileName);
        for (String s : VIDEO_FILE_TYPE) {
            if (s.equals(fileSuffix)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * 获取文件后缀
	 */
	public static String getFileSuffix(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}
	
	/**
	 * 获取文件名
	 */
	public static String getFileName(String fileName) {
		int dot = fileName.lastIndexOf('.'); 
		String filename = null;
		if (dot > -1) {
			 filename = fileName.substring(0, dot); 
		} 
		return filename;
	}
	
	
	/**
	 * 判断文件名是否为图片类型
	 */
	public static boolean isImgType(String fileSuffix) {
        for (String s : IMG_FILE_TYPE) {
            if (s.equals(fileSuffix)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * 根据文件后缀确定 Content-Type
	 */
	public static MediaType determineContentType(String fileSuffix) {
		// 使用 FileTypeUtil 判断文件类型
		if (FileTypeUtil.isImgType(fileSuffix)) {
			switch (fileSuffix) {
				case "jpg":
				case "jpeg":
					return MediaType.IMAGE_JPEG;
				case "png":
					return MediaType.IMAGE_PNG;
				case "gif":
					return MediaType.IMAGE_GIF;
				default:
					return MediaType.APPLICATION_OCTET_STREAM;
			}
		} else if (FileTypeUtil.bVideoFileType("example." + fileSuffix)) {
			switch (fileSuffix) {
				case "mp4":
					return MediaType.valueOf("video/mp4");
				case "avi":
					return MediaType.valueOf("video/x-msvideo");
				case "wmv":
					return MediaType.valueOf("video/x-ms-wmv");
				default:
					return MediaType.APPLICATION_OCTET_STREAM;
			}
		} else if (FileTypeUtil.bAudioFileType("example." + fileSuffix)) {
			switch (fileSuffix) {
				case "mp3":
					return MediaType.valueOf("audio/mpeg");
				case "wav":
					return MediaType.valueOf("audio/wav");
				default:
					return MediaType.APPLICATION_OCTET_STREAM;
			}
		} else if (FileTypeUtil.bLogFileType("example." + fileSuffix)) {
			switch (fileSuffix) {
				case "txt":
					return MediaType.TEXT_PLAIN;
				case "log":
					return MediaType.valueOf("text/plain");
				case "doc":
				case "docx":
					return MediaType.valueOf("application/msword");
				default:
					return MediaType.APPLICATION_OCTET_STREAM;
			}
		} else if ("pdf".equals(fileSuffix)) {
			return MediaType.APPLICATION_PDF;
		}

		// 默认类型
		return MediaType.APPLICATION_OCTET_STREAM;
	}
}

package cn.iecas.sampleset.utils;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 解压缩工具类
 */
@Slf4j
public class CompressUtil {
    private final static byte[] RAR_HEADER = {82,97,114,33}; //RAR格式的二进制头

    private final static byte[] ZIP_HEADER = {80,75,3,4}; //

    private final static byte[] ZIP_OLD_HEADER = {80,75,3,4};

    private final static byte[] TAR_HEADER = {117,115,116,97,114};

    private enum CompressFileType{
        ZIP,RAR,TAR
    }

    /**
     * 判断文件的压缩格式
     * @param filePath
     * @return
     */
    private static CompressFileType getCompressFileType(String filePath){
        byte[] fileHeader = new byte[4];
        byte[] tarFileHeader = new byte[5];
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(filePath,"r");
            randomAccessFile.read(fileHeader);
            randomAccessFile.seek(257);
            randomAccessFile.read(tarFileHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert randomAccessFile != null;
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (Arrays.equals(fileHeader,RAR_HEADER))
            return CompressFileType.RAR;

        if (Arrays.equals(fileHeader,ZIP_HEADER) || Arrays.equals(fileHeader, ZIP_OLD_HEADER))
            return CompressFileType.ZIP;

        if (Arrays.equals(tarFileHeader,TAR_HEADER))
            return CompressFileType.TAR;

        return null;
    }


    /**
     * 解压缩zip文件夹
     * @param srcFile
     * @param destFile
     * @return
     * @throws Exception
     */
    private static void unZipFile(File srcFile, File destFile) throws Exception {
        log.info("开始解压缩文件：{}",srcFile.getAbsolutePath());
        ZipFile srcZipFile = new ZipFile(srcFile.getAbsolutePath(), Charset.forName("GBK"));
        Enumeration<?> enumeration = srcZipFile.entries();
        while(enumeration.hasMoreElements()){
            ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
            String zipEntryName = FilenameUtils.normalize(zipEntry.getName());
            File entryFile = FileUtils.getFile(destFile.getAbsolutePath(),zipEntryName);

            if (zipEntry.isDirectory()){
                entryFile.mkdirs();
            }else {
                if (!entryFile.getParentFile().exists())
                    entryFile.mkdirs();
                InputStream inputStream = srcZipFile.getInputStream(zipEntry);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                byte[] buffer = new byte[1024];
                while( bis.read(buffer)!=-1){
                    bufferedOutputStream.write(buffer);
                }
                bufferedOutputStream.close();
                fileOutputStream.close();

            }
        }
        srcZipFile.close();
        log.info("解压缩文件：{}完成",srcFile.getAbsolutePath());
    }

    /**
     * 解压缩rar文件
     * @param srcFile
     * @param destFile
     * @return
     * @throws Exception
     */
    private static void unRarFile(File srcFile, File destFile) throws Exception {
        log.info("开始解压缩文件：{}",srcFile.getAbsolutePath());
        Archive archive = new Archive(new FileInputStream(srcFile));
        FileHeader fileHeader = archive.nextFileHeader();
        while(fileHeader!=null){
            String fileHeaderName = FilenameUtils.normalize(fileHeader.getFileNameString());
            File fileHeaderFile = FileUtils.getFile(destFile.getAbsolutePath(),fileHeaderName);
            if (fileHeader.isDirectory()){
                fileHeaderFile.mkdirs();
            }else {
                if (!fileHeaderFile.exists()){
                    fileHeaderFile.getParentFile().mkdirs();
                    fileHeaderFile.createNewFile();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(fileHeaderFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                archive.extractFile(fileHeader,bufferedOutputStream);
                fileOutputStream.close();
            }
            fileHeader = archive.nextFileHeader();
        }
        archive.close();
        log.info("解压缩文件：{}完成",srcFile.getAbsolutePath());
    }

    private static void unTarFile(File srcFile, File destFile) throws Exception {
        log.info("开始解压缩文件：{}",srcFile.getAbsolutePath());
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(srcFile));
        TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
        while(tarArchiveEntry!=null){
            File archiveEntryFile = FileUtils.getFile(destFile.getAbsolutePath(), FilenameUtils.normalize(tarArchiveEntry.getName()));
            if (tarArchiveEntry.isDirectory())
                archiveEntryFile.mkdirs();
            else {
                if (!archiveEntryFile.getParentFile().exists())
                    archiveEntryFile.mkdirs();

                byte[] buffer = new byte[1024];
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(archiveEntryFile));
                while(tarArchiveInputStream.read(buffer)!=-1){
                    bufferedOutputStream.write(buffer);
                }
                bufferedOutputStream.close();
            }
            tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
        }

        tarArchiveInputStream.close();
        log.info("解压缩文件：{}完成",srcFile.getAbsolutePath());
    }


    /*
    * 解压缩文件
    * */
    public static void decompress(String srcPath, String destPath, boolean delete) throws Exception
    {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        if (!srcFile.exists() || srcFile.length() ==0)
            throw new Exception("文件：" + srcFile.getName() + " 不存在");
        if (!destFile.exists())
            destFile.mkdirs();

        CompressFileType fileType = getCompressFileType(srcPath);
        if (fileType == null)
            throw new Exception("文件：" + srcFile.getName() + " 不是压缩格式");

        switch (fileType){
            case RAR:unRarFile(srcFile,destFile); break;
            case TAR:unTarFile(srcFile,destFile); break;
            case ZIP:unZipFile(srcFile,destFile); break;
        }

        if(delete)
            srcFile.delete();
    }



    /**
     * 压缩文件，指定输出流
     * @param srcDir 源文件(夹)路径
     * @param outputStream 输出流
     * @throws IOException
     */
    public static void toZip(String srcDir, OutputStream outputStream) throws IOException {
        File srcDirFile = new File(srcDir);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        compress(srcDirFile,zipOutputStream,srcDirFile.getName());
        zipOutputStream.close();
    }

    /**
     * 递归压缩文件
     * @param srcFile 源文件
     * @param zipOutputStream zip输出流
     * @param inZipName 文件对应的压缩包里的名称
     * @throws IOException
     */
    public static void compress(File srcFile, ZipOutputStream zipOutputStream, String inZipName) throws IOException {
        byte[] buffer = new byte[1024];
        if (srcFile.isFile()){
            int length = 0;
            zipOutputStream.putNextEntry(new ZipEntry(inZipName));
            FileInputStream fileInputStream = new FileInputStream(srcFile);

            while((length = fileInputStream.read(buffer))!=-1){
                zipOutputStream.write(buffer,0,length);
            }
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }else {
            zipOutputStream.putNextEntry(new ZipEntry(inZipName + "/"));
            zipOutputStream.closeEntry();

            File[] childFiles = srcFile.listFiles();
            for (File childFile : childFiles) {
                compress(childFile,zipOutputStream,inZipName + "/" + childFile.getName());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        decompress("D:\\Data\\traindata\\upload\\1\\sample_set\\21\\test.zip","D:\\data\\traindata\\upload\\1\\sample_set\\21",true);
    }

}
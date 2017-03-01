/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.system.parsing;

import org.springframework.stereotype.Service;

@Service
public class ParsingFilesServices {

//    public int getDurationForMP3(File file) {
//        MP3FileReader mp3FileReader = new MP3FileReader();
//
//        int duration = 0;
//        try {
//            duration = mp3FileReader.read(file).getAudioHeader().getTrackLength();
//            return duration;
//        } catch (Exception e) {
//            return duration;
//        }
//
//    }

//    public List<NameValueMap> getMetaDataForFile(File file) {
//
//        try {
//            InputStream input = new FileInputStream(file);
//            ContentHandler handler = new DefaultHandler();
//            Metadata metadata = new Metadata();
//            Parser parser = new Mp3Parser();
//            ParseContext parseCtx = new ParseContext();
//            parser.parse(input, handler, metadata, parseCtx);
//            input.close();
//
//            List<NameValueMap> metaInfo = new LinkedList<>();
//
//            String[] metadataNames = metadata.names();
//            for (String name : metadataNames) {
//                NameValueMap meta = new NameValueMap();
//                meta.setName(name);
//                meta.setValue(metadata.get(name));
//                metaInfo.add(meta);
//
//            }
//
//            return metaInfo;
//
//        } catch (Exception e) {
//
//            return null;
//        }
//    }

//    public List<List<NameValueMap>> convertXLSXToNameValueMap(InputStream file) {
//
//        List<String> headings = new ArrayList<>();
//        List<List<String>> allRows = new ArrayList<>();
//        List<String> cuurentRow;
//
////        FileInputStream file = null;
////        try {
////            file = new FileInputStream(file2);
////        } catch (FileNotFoundException e1) {
////            e1.printStackTrace();
////        }
//
//        XSSFWorkbook workbook = null;
//        try {
//            workbook = new XSSFWorkbook(file);
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        XSSFSheet sheet = workbook.getSheetAt(0);
//
////        for(Row row : sheet) {
////            for(int cn=0; cn<row.getLastCellNum(); cn++) {
////                // If the cell is missing from the file, generate a blank one
////                // (Works by specifying a MissingCellPolicy)
////                Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
////                // Print the cell for debugging
////                system.out.println("CELL: " + cn + " --> " + cell.toString());
////            }
//
//        for (Row row : sheet) {
////            Iterator<Cell> cellIterator = row.cellIterator();
//            cuurentRow = new ArrayList<>();
//
////            while (cellIterator.hasNext()) {
//            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
//                //                Cell cell = cellIterator.next();
//
//                Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
//
//                if (row.getRowNum() == 0) {
//                    headings.add(cell.getStringCellValue());
//                }
//
//                if (row.getRowNum() > 0) {
//
//                    switch (cell.getCellType()) {
//                        case Cell.CELL_TYPE_NUMERIC:
//                            cuurentRow.add(String.valueOf(cell.getNumericCellValue()));
//                            break;
//                        case Cell.CELL_TYPE_STRING:
//                            cuurentRow.add(cell.getStringCellValue());
//                            break;
//                        case Cell.CELL_TYPE_BLANK:
//                            cuurentRow.add("");
//                            break;
//                        default:
//                            cuurentRow.add("");
////                        case Cell.CE:
////                            cuurentRow.add(cell.getStringCellValue());
////                            break;
//
////                        case Cell.CELL_TYPE_BOOLEAN:
////                            cuurentRow.add(cell.getBooleanCellValue());
////                            break;
//                    }
//                }
//            }
//
//            if (row.getRowNum() > 0) {
//                if (allRows.size() > 0 && (cuurentRow.size() < allRows.get(0).size())) {
//                    for (int p = cuurentRow.size(); p < allRows.get(0).size(); p++) {
//                        cuurentRow.add(null);
//                    }
//                }
//
//                allRows.add(cuurentRow);
//            }
//        }
//
////        try {
////            file.close();
////        } catch (IOException e1) {
////            e1.printStackTrace();
////        }
////        file();
//
//        List<List<NameValueMap>> listList = new ArrayList<>();
//
//        for (List<String> currentRowString : allRows) {
//            List<NameValueMap> nameValueMaps = new ArrayList<>();
//            for (int i = 0; i < headings.size(); i++) {
//                NameValueMap nameValueMap = new NameValueMap();
//                nameValueMap.setName(headings.get(i));
//                nameValueMap.setValue(currentRowString.get(i));
//                nameValueMaps.add(nameValueMap);
//            }
//            listList.add(nameValueMaps);
//        }
//        return listList;
//    }

}

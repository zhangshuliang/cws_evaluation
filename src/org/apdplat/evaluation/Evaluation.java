/**
 * 
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.apdplat.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 通用评估逻辑
 * @author 杨尚川
 */
public abstract class Evaluation {
    protected final String testText = "data/test-text.txt";
    protected final String standardText = "data/standard-text.txt";
    public abstract List<EvaluationResult> run() throws Exception;
    /**
     * 分词效果评估
     * @param resultText 实际分词结果文件路径
     * @param standardText 标准分词结果文件路径
     * @return 评估结果
     * @throws java.lang.Exception
     */
    protected EvaluationResult evaluate(String resultText, String standardText) throws Exception {
        int perfectLineCount=0;
        int wrongLineCount=0;
        int perfectCharCount=0;
        int wrongCharCount=0;
        try(BufferedReader resultReader = new BufferedReader(new InputStreamReader(new FileInputStream(resultText),"utf-8"));
            BufferedReader standardReader = new BufferedReader(new InputStreamReader(new FileInputStream(standardText),"utf-8"))){
            String result;
            while( (result = resultReader.readLine()) != null ){
                result = result.trim();
                String standard = standardReader.readLine().trim();
                if(result.equals("")){
                    continue;
                }
                if(result.equals(standard)){
                    //分词结果和标准一模一样
                    perfectLineCount++;
                    perfectCharCount+=standard.replaceAll("\\s+", "").length();
                }else{
                    //分词结果和标准不一样
                    wrongLineCount++;
                    wrongCharCount+=standard.replaceAll("\\s+", "").length();
                }
            }
        }
        int totalLineCount = perfectLineCount+wrongLineCount;
        int totalCharCount = perfectCharCount+wrongCharCount;
        EvaluationResult er = new EvaluationResult();
        er.setPerfectCharCount(perfectCharCount);
        er.setPerfectLineCount(perfectLineCount);
        er.setTotalCharCount(totalCharCount);
        er.setTotalLineCount(totalLineCount);
        er.setWrongCharCount(wrongCharCount);
        er.setWrongLineCount(wrongLineCount);     
        return er;
    }
    /**
     * 对文件进行分词
     * @param input 输入文件
     * @param output 输出文件
     * @param segmenter 对文本进行分词的逻辑
     * @return 分词速率
     * @throws Exception 
     */
    protected float segFile(final String input, final String output, final Segmenter segmenter) throws Exception{
        float rate = 0;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input),"utf-8"));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"utf-8"))){
            long size = Files.size(Paths.get(input));
            System.out.println("size:"+size);
            System.out.println("文件大小："+(float)size/1024/1024+" MB");
            int textLength=0;
            int progress=0;
            long start = System.currentTimeMillis();
            String line = null;
            while((line = reader.readLine()) != null){
                if("".equals(line.trim())){
                    writer.write("\n");
                    continue;
                }
                textLength += line.length();
                writer.write(segmenter.seg(line));
                writer.write("\n");
                progress += line.length();
                if( progress > 500000){
                    progress = 0;
                    System.out.println("分词进度："+(int)(textLength*2.99/size*100)+"%");
                }
            }
            long cost = System.currentTimeMillis() - start;
            rate = textLength/(float)cost;
            System.out.println("字符数目："+textLength);
            System.out.println("分词耗时："+cost+" 毫秒");
            System.out.println("分词速度："+rate+" 字符/毫秒");
        }
        return rate;
    }
}
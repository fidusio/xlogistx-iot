package io.xlogistx.iot.net.util;

import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.server.util.RuntimeUtil;
import org.zoxweb.shared.data.RuntimeResultDAO;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVStringList;
import org.zoxweb.shared.util.ParamUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

public class CommandLineMatch {
    public static void main(String... args) {
        try {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            String command = params.stringValue("command", false);
            String lineMatch = params.stringValue("match", false);
            String valueName = params.stringValue("name", false);
            String output = params.stringValue("output", true);
            RuntimeResultDAO result = RuntimeUtil.runAndFinish(command);
            String response = result.getOutputData();
            BufferedReader br = new BufferedReader(new StringReader(response));

            String line;
            NVStringList matches = new NVStringList();
            while ((line = br.readLine()) != null) {
                if (line.contains(lineMatch)) {
                    String[] parsedLine = line.split(" ");
                    for (int i = 0; i < parsedLine.length; i++) {
                        if (parsedLine[i].equalsIgnoreCase(valueName)) {
                            if (i + 1 < parsedLine.length) {
                                matches.add(parsedLine[i + 1]);
                                break;
                            }
                        }
                    }
                }
            }
            NVGenericMap nvgm = new NVGenericMap();
            matches.setName("sessions");
            nvgm.add(matches);
            String json = GSONUtil.toJSONDefault(nvgm, true);
            System.out.println(json);
            System.out.println("we have " + matches.getValue().size() + " matches");

            if (output != null) {
                if (!output.endsWith(".json")) {
                    output += ".json";
                }

                String comment = "// This is a generated file from " + command + "\n" +
                        "// for " + lineMatch + "\n";
                json = comment + json;

                IOUtil.writeToFile(new File(output), json);

                nvgm = GSONUtil.fromJSONDefault(json, NVGenericMap.class);
                System.out.println(nvgm);
            }

        } catch (Exception e) {
            e.printStackTrace();

            System.err.println("usage: command=os-command match=line-match name=of-the-nvpair [output=fileoutput]");
        }
    }

}

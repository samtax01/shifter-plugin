/*
 * Copyright 2011-2017 Kay Stenschke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kstenschke.shifter.models.shiftableTypes;

import com.kstenschke.shifter.utils.UtilsTextual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.trim;

/**
 * Cascading Stylesheet - sort all attributes in all selectors alphabetically
 */
public class Css {

    /**
     * @param  value
     * @return String
     */
    public static String getShifted(String value) {
        if (!value.contains("{") && !value.contains("}")) {
            return null;
        }

        String attributeGroups[]       = value.split("([^\r\n,{}]+)(,(?=[^}]*\\{)|\\s*\\{)");
        String attributeGroupsSorted[] = new String[attributeGroups.length];

        int indexMatch = 0;
        for (String attributeGroup : attributeGroups) {
            if (indexMatch > 0) {
                List<String> lines = splitAttributesIntoLines(attributeGroup);
                lines              = prepareAttributeLineForConcat(lines);

                List<String> linesSorted = UtilsTextual.sortLines(lines, true);
                linesSorted              = moveVendorAttributeLinesToEnd(linesSorted);
                attributeGroupsSorted[indexMatch] = UtilsTextual.rtrim(UtilsTextual.joinLines(linesSorted).toString());

                value = value.replaceFirst(attributeGroup, "###SHIFTERMARKER" + indexMatch + "###");
            }
            indexMatch++;
        }
        for (int indexMarker = 1; indexMarker < indexMatch; indexMarker++) {
            value = value.replaceFirst(
                    "###SHIFTERMARKER" + indexMarker + "###",
                    (indexMarker == 1 ? "\n" : "") + attributeGroupsSorted[indexMarker]);
        }

        return value;
    }

    private static List<String> splitAttributesIntoLines(String str) {
        return Arrays.asList(str.split("\\n"));
    }

    /**
     * Ensure that all attribute lines end w/ ";\n"
     *
     * @param  lines
     * @return List<String>
     */
    private static List<String> prepareAttributeLineForConcat(List<String> lines) {
        int index = 0;
        for (String line : lines) {
            String trimmed = trim(line);
            if (!trimmed.isEmpty() && !trimmed.equals("}")) {
                line = UtilsTextual.rtrim(line);

                if (!line.endsWith(";")) {
                    line = line + ";";
                }
                line = line + "\n";
            }

            lines.set(index, line);
            index++;
        }

        return lines;
    }

    private static List<String> moveVendorAttributeLinesToEnd(List<String> lines) {
        List<String> linesSorted = new ArrayList<String>(lines.size());
        String lastLine = "";

        // Add non-vendor attributes into sorted list
        for (String line : lines) {
            String trimmed = trim(line);
            if (!trimmed.startsWith("-")) {
                if (trimmed.equals("}")) {
                    lastLine = line;
                } else {
                    linesSorted.add(line);
                }
            }
        }
        // Add vendor-attributes at end
        for (String line : lines) {
            if (trim(line).startsWith("-")) {
                linesSorted.add(line);
            }
        }

        linesSorted.add(lastLine);

        return linesSorted;
    }
}
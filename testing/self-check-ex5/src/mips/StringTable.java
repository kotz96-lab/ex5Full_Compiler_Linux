package mips;

import java.util.*;

/**
 * String Table
 *
 * Manages string literals for MIPS generation.
 * - Collects all unique strings
 * - Assigns labels (string_0, string_1, ...)
 * - Emits .data section entries
 */
public class StringTable
{
    private Map<String, String> stringToLabel;
    private int stringCounter;

    public StringTable()
    {
        this.stringToLabel = new HashMap<>();
        this.stringCounter = 0;
    }

    /**
     * Add a string literal and get its label
     *
     * If string already exists, returns existing label.
     * Otherwise, creates new label.
     *
     * @param value the string value
     * @return label for this string (e.g., "string_0")
     */
    public String addString(String value)
    {
        if (stringToLabel.containsKey(value)) {
            return stringToLabel.get(value);
        }

        String label = String.format("string_%d", stringCounter++);
        stringToLabel.put(value, label);
        return label;
    }

    /**
     * Get label for a string
     *
     * @param value the string value
     * @return label, or null if string not in table
     */
    public String getLabel(String value)
    {
        return stringToLabel.get(value);
    }

    /**
     * Emit all strings to data section
     */
    public void emitAllStrings(MipsGenerator gen)
    {
        for (Map.Entry<String, String> entry : stringToLabel.entrySet()) {
            gen.emitDataString(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get number of strings in table
     */
    public int size()
    {
        return stringToLabel.size();
    }
}

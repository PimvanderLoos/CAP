/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.text;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextTest
{
    final @NonNull ColorScheme colorScheme = ColorScheme.builder()
                                                        .addStyle(TextType.REGULAR_TEXT, "!", "?")
                                                        .addStyle(TextType.COMMAND, "~~", "||")
                                                        .addStyle(TextType.HEADER, "___", "---")
                                                        .build();

    @Test
    void subsection()
    {
        final @NonNull Text text = new Text(colorScheme).add("123456789", TextType.HEADER);

        Assertions.assertEquals("123", text.subsection(0, 3).toPlainString());
        Assertions.assertEquals("456", text.subsection(3, 6).toPlainString());
        Assertions.assertEquals("789", text.subsection(6, 9).toPlainString());

        Assertions.assertThrows(RuntimeException.class, () -> text.subsection(-1, 4));
        Assertions.assertThrows(RuntimeException.class, () -> text.subsection(0, 11));
        Assertions.assertThrows(RuntimeException.class, () -> text.subsection(4, 3));
        Assertions.assertThrows(RuntimeException.class, () -> text.subsection(4, 4));
    }

    @Test
    void styledSubsection()
    {
        final @NonNull Text text = new Text(colorScheme)
            .add("123", TextType.REGULAR_TEXT)
            .add("456", TextType.COMMAND)
            .add("789", TextType.HEADER);

        Assertions.assertEquals("!1?", text.subsection(0, 1).toString());
        Assertions.assertEquals("!123?~~4||", text.subsection(0, 4).toString());
        Assertions.assertEquals("~~56||___789---", text.subsection(4, 9).toString());
    }

    @Test
    void toStringTest()
    {
        final @NonNull Text textA = new Text(colorScheme);
        final @NonNull Text textB = new Text(colorScheme);

        textA.add("abc", TextType.REGULAR_TEXT);
        textB.add("def", TextType.COMMAND);
        Assertions.assertEquals("!abc?~~def||", textA.add(textB).toString());
    }

    @Test
    void add()
    {
        final @NonNull Text textA = new Text(colorScheme).add("abcdef");
        final @NonNull Text textB = new Text(colorScheme).add("ghifjk");

        Assertions.assertEquals("abcdefghifjk", textA.add(textB).toPlainString());
        Assertions.assertEquals("ghifjkghifjk", textB.add(textB).toString());
    }

    @Test
    void prepend()
    {
        final @NonNull Text textA = new Text(colorScheme).add("abc", TextType.REGULAR_TEXT);
        final @NonNull Text textB = new Text(colorScheme).add("def", TextType.COMMAND);

        Assertions.assertEquals("~~def||!abc?", textA.prepend(textB).toString());
    }

    @Test
    void addStyled()
    {
        final @NonNull Text textA = new Text(colorScheme);
        final @NonNull Text textB = new Text(colorScheme);

        textA.add("abc", TextType.REGULAR_TEXT);
        textB.add("def", TextType.COMMAND);

        Assertions.assertEquals(3, textA.getLength());
        Assertions.assertEquals(3 + 2, textA.getStyledLength()); // +2 for the style.

        Assertions.assertEquals(3, textB.getLength());
        Assertions.assertEquals(3 + 4, textB.getStyledLength()); // +4 for the style.

        final @NonNull Text textAB = new Text(textA).add(textB);
        Assertions.assertEquals("abcdef", textAB.toPlainString());
    }
}

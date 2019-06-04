/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core;

import com.opencsv.CSVReader;
import org.ta4j.core.data.Period;
import org.ta4j.core.num.PrecisionNum;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class build a Ta4j time series from a CSV file containing trades.
 */
public class CsvTradesLoader {

    /**
     * @return a time series from Bitstamp (bitcoin exchange) trades
     */
    public static TimeSeries loadSeries(String fileName, Period period) {

        TimeSeries timeSeries = new BaseTimeSeries.SeriesBuilder().withName("series").withNumTypeOf(PrecisionNum.class).build();
        Duration duration = period.getDuration();

        // Reading all lines of the CSV file
        InputStream stream = CsvTradesLoader.class.getClassLoader().getResourceAsStream("data/" + fileName);
        CSVReader csvReader = null;
        List<String[]> lines = null;
        try {
            csvReader = new CSVReader(new InputStreamReader(stream, Charset.forName("UTF-8")), ',');
            lines = csvReader.readAll();
            lines.remove(0); // Removing header line
        } catch (IOException ioe) {
            Logger.getLogger(CsvTradesLoader.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", ioe);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ignored) {
                }
            }
        }

        if ((lines != null) && !lines.isEmpty()) {
            for (String[] tradeLine : lines) {
                timeSeries.addBar(new BaseBar(
                        duration,
                        Instant.parse(tradeLine[5]).plus(duration).atZone(ZoneOffset.UTC),
                        PrecisionNum.valueOf(tradeLine[0]),
                        PrecisionNum.valueOf(tradeLine[1]),
                        PrecisionNum.valueOf(tradeLine[2]),
                        PrecisionNum.valueOf(tradeLine[3]),
                        PrecisionNum.valueOf(tradeLine[4]),
                        PrecisionNum.valueOf(0)));

            }
        }
        return timeSeries;
    }
}

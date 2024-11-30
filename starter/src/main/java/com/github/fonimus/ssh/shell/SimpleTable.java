/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.github.fonimus.ssh.shell;

 import lombok.Builder;
 import lombok.Data;
 import lombok.NonNull;
 import lombok.Singular;
 import org.springframework.shell.table.Aligner;
 import org.springframework.shell.table.BorderStyle;
 import org.springframework.shell.table.TableBuilder;
 import org.springframework.shell.table.TableModelBuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Simple data builder, with header names, and list of lines, containing map with header names.
  * Optionally set aligner, and style. Now includes dynamic methods for better modularization.
  */
 @Data
 @Builder
 public class SimpleTable {
 
     @Singular
     private List<String> columns;
 
     @Builder.Default
     private boolean displayHeaders = true;
 
     @Singular
     private List<Aligner> headerAligners;
 
     @NonNull
     @Singular
     private List<List<Object>> lines;
 
     @Singular
     private List<Aligner> lineAligners;
 
     @Builder.Default
     private boolean useFullBorder = true;
 
     @Builder.Default
     private BorderStyle borderStyle = BorderStyle.fancy_light;
 
     private SimpleTableBuilderListener tableBuilderListener;
 
     /**
      * Adds a row to the table.
      *
      * @param row A list of objects representing a row. Must match the number of columns.
      */
     public void addRow(List<Object> row) {
         if (columns != null && row.size() != columns.size()) {
             throw new IllegalArgumentException("Row size must match the number of columns.");
         }
         if (lines == null) {
             lines = new ArrayList<>();
         }
         lines.add(row);
     }
 
     /**
      * Renders the table into a string format using Spring Shell's TableBuilder.
      *
      * @return String representation of the table.
      */
     public String render() {
         TableModelBuilder<Object> modelBuilder = new TableModelBuilder<>();
 
         // Add headers if required
         if (displayHeaders && columns != null) {
             modelBuilder.addRow();
             columns.forEach(modelBuilder::addValue);
         }
 
         // Add rows
         if (lines != null) {
             for (List<Object> line : lines) {
                 modelBuilder.addRow();
                 line.forEach(modelBuilder::addValue);
             }
         }
 
         TableBuilder tableBuilder = new TableBuilder(modelBuilder.build());
         tableBuilder.addFullBorder(borderStyle); // Corrected line
 
         if (tableBuilderListener != null) {
             tableBuilderListener.onBuilt(tableBuilder);
         }
 
         return tableBuilder.build().render(80);
     }
 
     /**
      * Resets the table by clearing all rows.
      */
     public void reset() {
         if (lines != null) {
             lines.clear();
         }
     }
 
     /**
      * Listener to add some properties to table builder before it is rendered.
      */
     @FunctionalInterface
     public interface SimpleTableBuilderListener {
 
         /**
          * Method called before render
          *
          * @param tableBuilder table builder
          */
         void onBuilt(TableBuilder tableBuilder);
     }
 }
 
package org.segin.bfinterpreter;

/*
 * Copyright 2014 Kirn Gill II <segin2005@gmail.com>
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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HexDumpAdapter extends RecyclerView.Adapter<HexDumpAdapter.ViewHolder> {

    private final Tape tape;
    private int pointer;
    private final OnByteClickListener listener;
    private static final int BYTES_PER_LINE = 16;

    public interface OnByteClickListener {
        void onByteClick(int address, char currentValue);
    }

    public HexDumpAdapter(Tape tape, OnByteClickListener listener) {
        this.tape = tape;
        this.listener = listener;
    }

    public Tape getTape() {
        return tape;
    }

    public void updatePointer(int pointer) {
        this.pointer = pointer;
        notifyDataSetChanged(); // Optimization: Use DiffUtil or range change if pointer moves slightly
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Offset
        TextView offsetView = new TextView(context);
        offsetView.setTypeface(Typeface.MONOSPACE);
        offsetView.setTextColor(Color.parseColor("#000080"));
        offsetView.setPadding(8, 4, 16, 4);
        rowLayout.addView(offsetView);

        // Bytes container
        LinearLayout bytesLayout = new LinearLayout(context);
        bytesLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < BYTES_PER_LINE; i++) {
            TextView byteView = new TextView(context);
            byteView.setTypeface(Typeface.MONOSPACE);
            byteView.setGravity(Gravity.CENTER);
            byteView.setPadding(4, 4, 4, 4);
            byteView.setMinWidth(50); // Ensure clickable area
            bytesLayout.addView(byteView);
        }
        // Spacer
        TextView spacer = new TextView(context);
        spacer.setText("  ");
        bytesLayout.addView(spacer);
        rowLayout.addView(bytesLayout);

        // ASCII
        TextView asciiView = new TextView(context);
        asciiView.setTypeface(Typeface.MONOSPACE);
        asciiView.setTextColor(Color.parseColor("#666666"));
        asciiView.setPadding(16, 4, 8, 4);
        rowLayout.addView(asciiView);

        return new ViewHolder(rowLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int startAddress = position * BYTES_PER_LINE;
        holder.bind(startAddress);
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil((double) tape.getSize() / BYTES_PER_LINE);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView offsetView;
        private final LinearLayout bytesLayout;
        private final TextView asciiView;

        public ViewHolder(View itemView) {
            super(itemView);
            LinearLayout layout = (LinearLayout) itemView;
            offsetView = (TextView) layout.getChildAt(0);
            bytesLayout = (LinearLayout) layout.getChildAt(1);
            asciiView = (TextView) layout.getChildAt(2);
        }

        public void bind(int startAddress) {
            offsetView.setText(String.format("%04X", startAddress));

            StringBuilder asciiBuilder = new StringBuilder();
            int tapeSize = tape.getSize();

            for (int i = 0; i < BYTES_PER_LINE; i++) {
                final int address = startAddress + i;
                TextView byteView = (TextView) bytesLayout.getChildAt(i);

                if (address < tapeSize) {
                    final char val = tape.get(address);
                    byteView.setText(String.format("%02X", (int)val));
                    byteView.setVisibility(View.VISIBLE);

                    if (address == pointer) {
                        byteView.setBackgroundColor(Color.parseColor("#000080"));
                        byteView.setTextColor(Color.WHITE);
                    } else {
                        byteView.setBackgroundColor(Color.TRANSPARENT);
                        byteView.setTextColor(Color.parseColor("#333333"));
                    }

                    byteView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.onByteClick(address, val);
                            }
                        }
                    });

                    if (val >= 32 && val <= 126) {
                        asciiBuilder.append(val);
                    } else {
                        asciiBuilder.append('.');
                    }
                } else {
                    byteView.setVisibility(View.INVISIBLE);
                    asciiBuilder.append(' ');
                }
            }
            asciiView.setText(asciiBuilder.toString());
        }
    }
}

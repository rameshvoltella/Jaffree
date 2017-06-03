package com.github.kokorin.jaffree;

import com.github.kokorin.jaffree.cli.Input;
import com.github.kokorin.jaffree.cli.Option;
import com.github.kokorin.jaffree.cli.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FFmpeg extends Executable<Void> {
    private List<Input> inputs;
    private List<Output> outputs;
    private List<Option> additionalOptions;
    private boolean overwriteOutput;

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpeg.class);

    public FFmpeg(Path executablePath) {
        super(executablePath);
    }

    public FFmpeg addInput(Input input) {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }

        inputs.add(input);

        return this;
    }

    public FFmpeg addInput(Option option) {
        if (additionalOptions == null) {
            additionalOptions = new ArrayList<>();
        }

        additionalOptions.add(option);

        return this;
    }

    public FFmpeg addOutput(Output output) {
        if (outputs == null) {
            outputs = new ArrayList<>();
        }

        outputs.add(output);

        return this;
    }


    /**
     * Whether to overwrite or to stop. False by default.
     * @param overwriteOutput true if forcibly overwrite, false if to stop
     * @return this
     */
    public FFmpeg setOverwriteOutput(boolean overwriteOutput) {
        this.overwriteOutput = overwriteOutput;
        return this;
    }

    @Override
    protected List<Option> buildOptions() {
        List<Option> result = new ArrayList<>();

        if (inputs != null) {
            for (Input input : inputs) {
                List<Option> inputOptions = input.buildOptions();
                if (inputOptions != null) {
                    result.addAll(inputOptions);
                }
            }
        }

        if (overwriteOutput) {
            //Overwrite output files without asking.
            result.add(new Option("-y"));
        } else {
            // Do not overwrite output files, and exit immediately if a specified output file already exists.
            result.add(new Option("-n"));
        }

        if (additionalOptions != null) {
            result.addAll(additionalOptions);
        }

        if (outputs != null) {
            for (Output output : outputs) {
                List<Option> outputOptions = output.buildOptions();
                if (outputOptions != null) {
                    result.addAll(outputOptions);
                }
            }
        }


        return result;
    }

    @Override
    protected Void parseStdOut(InputStream stdOut) {
        //just read stdErr fully
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                LOGGER.info("stdout - " + line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    protected void parseStdErr(InputStream stdErr) throws Exception {
        //just read stdErr fully
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdErr));
        String line;
        while ((line = reader.readLine()) != null) {
            LOGGER.info("stderr - " + line);
        }
    }

    public static FFmpeg atPath(Path pathToDir) {
        String os = System.getProperty("os.name");
        if (os == null) {
            throw new RuntimeException("Failed to detect OS");
        }

        Path executable;
        if (os.toLowerCase().contains("win")) {
            executable = pathToDir.resolve("ffmpeg.exe");
        } else {
            executable = pathToDir.resolve("ffmpeg");
        }

        return new FFmpeg(executable);
    }
}

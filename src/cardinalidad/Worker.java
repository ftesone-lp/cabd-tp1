package cardinalidad;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

public class Worker extends Configured implements Tool
{
    String baseDir;

    private Job setupJob(String[] args) throws IOException
    {
        return setupJob(args, getConf());
    }

    public Job setupJob(String[] args, Configuration conf) throws IOException
    {
        Job job = new Job(conf, "Cardinalidad");

        job.setJarByClass(Worker.class);

        // configure Mapper
        job.setMapperClass(Mapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(LongWritable.class);

        // configure Combiner
        job.setCombinerClass(Reducer.class);

        // configure Reducer
        job.setReducerClass(Reducer.class);

        // configure output
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(LongWritable.class);

        // configure input and output formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // configure input and output folders
        FileSystem fs = FileSystem.get(conf);
        String inputDir = args[0];
        String outputDir = args[1];
        if (fs.exists(new Path(outputDir))) {
            fs.delete(new Path(outputDir), true);
        }

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));

        return job;
    }

    @Override
    public int run(String[] args) throws Exception
    {
        Job job = setupJob(args);

        boolean success = job.waitForCompletion(true);

        if (!success) {
            System.out.println("Error job");

            return -1;
        }

        return 0;
    }
}

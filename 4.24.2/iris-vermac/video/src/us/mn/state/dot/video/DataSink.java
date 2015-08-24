package us.mn.state.dot.video;

public interface DataSink {

	/** Flush the data down the sink */
	public void flush(byte[] data);
}

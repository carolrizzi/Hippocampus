package uk.ac.kent.cs;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.FactHandle;

import uk.ac.kent.cs.model.event.Adrenaline;
import uk.ac.kent.cs.model.event.EnvironmentalCue;
import uk.ac.kent.cs.model.situation.Situation;

public class Hippocampus {

	private Vector<Object> data = new Vector<Object>();
	private static KnowledgeBase kbase = null;
	private static StatefulKnowledgeSession ksession = null;
	public static final double ADRENALINE_THRESHOLD = 0.5;
	
	public static void main (String [] args) throws Exception {}
	
	public Hippocampus() throws Exception {
		System.out.println("Creating hippocampus");
		this.initKnowledgeBase();
		this.initKownledgeSession();
		System.out.println("Hippocampus created");
	}
	
    private void initKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        try{
        	kbuilder.add(ResourceFactory.newClassPathResource("situation.drl"), ResourceType.DRL);
        }catch(Exception e){
        	kbuilder.add(ResourceFactory.newClassPathResource("resources/situation.drl"), ResourceType.DRL);
        }
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption( EventProcessingOption.STREAM );
        kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
    
    private void initKownledgeSession () throws Exception {
    	if(kbase == null) throw new Exception ("KnowledgeBase cannot be null.");
    	KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
    	config.setOption(ClockTypeOption.get("realtime"));
    	ksession = kbase.newStatefulKnowledgeSession(config, null);
    	ksession.setGlobal("adrThreshold", ADRENALINE_THRESHOLD);
    	ksession.setGlobal("hippocampus", this);
    }
    
    public synchronized void addSituationListener (SituationListener listener) {
    	this.data.addElement(listener);
    }
    
    public synchronized void removeSituationListener (SituationListener listener) {
    	this.data.removeElement(listener);
    }
    
    public void insertEnvironmentalCue (byte [] features) {
    	EnvironmentalCue cue = new EnvironmentalCue(features);
    	this.insert(cue);
    }
    
    public void insertAdrenaline (double level) {
    	Adrenaline adrenaline = new Adrenaline(level);
    	this.insert(adrenaline);
    }
    
    public interface SituationListener extends EventListener {
    	void listenToSituation (SituationProjection projection);
    }
    
    public class SituationProjection extends EventObject {
		private static final long serialVersionUID = 1L;
		private byte [] situation;

		public SituationProjection(Object source, byte [] situation) {
			super(source);
			this.situation = situation;
		}
		
		public byte[] getSituation () {
			return this.situation;
		}
    }
    
    @SuppressWarnings("unchecked")
	public <T extends Situation> void projectSituation (T situation) {
    	try {
    		byte [] serObj = serialize(situation);
    		Vector<Object> copy;
    		synchronized (this) {
				copy = (Vector<Object>) this.data.clone();
			}
    		SituationProjection projection = new SituationProjection(this, serObj);
    		for (Object c : copy){
    			((SituationListener)c).listenToSituation(projection);;
    		}
			System.err.println(situation.getClass().toString() + ": " + serObj.toString());
//			Runtime.getRuntime().exec("matlabscript.m " + serObj);
		} catch (Exception e) {
			System.err.println("Could not project DangerSituation.");
			e.printStackTrace();
		}
    }
    
    public void ping (String str) {
    	System.out.println("Hey! I hear you! Here is your string: " + str);
    }
    
    private FactHandle insert (Object object) {
    	FactHandle fh = ksession.insert(object);
    	ksession.fireAllRules();
    	return fh;
    }
    
    private static byte [] serialize (Object obj) throws Exception {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutput out = null;
		out = new ObjectOutputStream(bos);   
		out.writeObject(obj);
		byte [] serializedObj = bos.toByteArray();
		out.close();
		bos.close();
		return serializedObj; 	
    }
    
//    private void update (FactHandle fh, Object object) {
//    	ksession.update(fh, object);
//    	ksession.fireAllRules();
//    }

//    public void retract (FactHandle fh) {
//    	ksession.retract(fh);
//    	ksession.fireAllRules();
//    }
	
    public void disposeSession () {
    	ksession.dispose();
    }
}

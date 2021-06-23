package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.core.common.DroolsObjectOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class SerializationOfOrAndEvalTest {

	private static final String PROCESS_ID = "example";
	
	private KieBase kieBase;
	
	@Before
	public void setUp() {
		
		Path resources = Paths.get("src", "test", "resources");
		KieServices services = KieServices.Factory.get();
		KieFileSystem kfs = services.newKieFileSystem();

		Resource drl = services.getResources()
				.newFileSystemResource(
						resources.resolve("drl/rules.drl").toFile());
		
		Resource rf = services.getResources()
				.newFileSystemResource(
						resources.resolve("rf/ruleflow.rf").toFile());
		kfs.write(drl);
		kfs.write(rf);
		
		services.newKieBuilder(kfs).buildAll();
		KieContainer container = services.newKieContainer(
				services.getRepository().getDefaultReleaseId());
		
		this.kieBase = container.getKieBase();
	}
	
	@After
	public void cleanUp() {
		this.kieBase = null;
	}
	
	@Test
	public void orAndEval() throws IOException, ClassNotFoundException {
		Path filePath = Paths.get("orAndEval.kieBase");
		
		try(OutputStream fos = Files.newOutputStream(filePath);
				ObjectOutputStream oos = new DroolsObjectOutputStream(fos)){
			
			oos.writeObject(this.kieBase);
		}
		
		KieBase loaded = this.loadSerialized(filePath);
		
		KieSession session = loaded.newKieSession();

		ClassWithValue c = new ClassWithValue();
		session.insert(c);
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
	}

	@SuppressWarnings("unchecked")
	private <T> T loadSerialized(Path filePath) throws IOException, ClassNotFoundException {
		try(InputStream fis = Files.newInputStream(filePath);
				ObjectInputStream ois = new DroolsObjectInputStream(fis)){
			
			return (T) ois.readObject();
		}
	}
}

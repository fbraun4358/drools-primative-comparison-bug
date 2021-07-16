package com.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.drools.modelcompiler.ExecutableModelProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class ExecutableModelWithUpdateFromCollectTests {

	private static final String PROCESS_ID = "example";
	
	private KieServices services;
	private KieFileSystem kfs;
	
	@Before
	public void setUp() {
		
		Path resources = Paths.get("src", "test", "resources");
		this.services = KieServices.Factory.get();
		this.kfs = services.newKieFileSystem();

		Resource drl = this.services.getResources()
				.newFileSystemResource(
						resources.resolve("drl/rules.drl").toFile());
		
		Resource rf = this.services.getResources()
				.newFileSystemResource(
						resources.resolve("rf/ruleflow.rf").toFile());
		this.kfs.write(drl);
		this.kfs.write(rf);
		
	}
	
	@After
	public void cleanUp() {
		this.services = null;
		this.kfs = null;
	}
	
	@Test
	public void withExecutableModel() throws IOException, ClassNotFoundException {
		this.services.newKieBuilder(this.kfs).buildAll(ExecutableModelProject.class);
		
		KieContainer container = services.newKieContainer(
				services.getRepository().getDefaultReleaseId());
		
		KieBase kbase = container.getKieBase();
		
		KieSession session = kbase.newKieSession();

		ClassWithValues c = new ClassWithValues();
		session.insert(c);
		session.insert("one");
		session.insert("two");
		session.insert("three");
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
	}
	
	@Test
	public void withoutExecutableModel() {
		this.services.newKieBuilder(this.kfs).buildAll();
		
		KieContainer container = services.newKieContainer(
				services.getRepository().getDefaultReleaseId());
		
		KieBase kbase = container.getKieBase();
		
		KieSession session = kbase.newKieSession();

		ClassWithValues c = new ClassWithValues();
		session.insert(c);
		session.insert("one");
		session.insert("two");
		session.insert("three");
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
	}
}

package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.drools.modelcompiler.ExecutableModelProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

@RunWith(Parameterized.class)
public class ExecutableModelPrimativeConversionErrorsTests {
	
	private static final String PROCESS_ID = "example";
	
	@Parameterized.Parameters(name="Method Returns {0}, test against {1}")
	public static Collection<Object[]> data(){
		   Object[][] data = new Object[][]{
			   {Float.class, Float.class,   4f,       5f, 6f},
			   {Float.class, Double.class,  4d,       5f, 6f},
			   {Float.class, Integer.class, 4,        5f, 6f},
			   {Float.class, Short.class,   (short)4, 5f, 6f},
			   {Float.class, Byte.class,    (byte)4,  5f, 6f},

			   {Double.class, Float.class,   4f,       5d, 6d},
			   {Double.class, Double.class,  4d,       5d, 6d},
			   {Double.class, Integer.class, 4,        5d, 6d},
			   {Double.class, Short.class,   (short)4, 5d, 6d},
			   {Double.class, Byte.class,    (byte)4,  5d, 6d},
			   
			   {Integer.class, Float.class,   4f,       5, 6},
			   {Integer.class, Double.class,  4d,       5, 6},
			   {Integer.class, Integer.class, 4,        5, 6},
			   {Integer.class, Short.class,   (short)4, 5, 6},
			   {Integer.class, Byte.class,    (byte)4,  5, 6},
			   
			   {Short.class, Float.class,   4f,       (short)5, (short)6},
			   {Short.class, Double.class,  4d,       (short)5, (short)6},
			   {Short.class, Integer.class, 4,        (short)5, (short)6},
			   {Short.class, Short.class,   (short)4, (short)5, (short)6},
			   {Short.class, Byte.class,    (byte)4,  (short)5, (short)6},
			   
			   {Byte.class, Float.class,   4f,       (byte)5, (byte)6},
			   {Byte.class, Double.class,  4d,       (byte)5, (byte)6},
			   {Byte.class, Integer.class, 4,        (byte)5, (byte)6},
			   {Byte.class, Short.class,   (short)4, (byte)5, (byte)6},
			   {Byte.class, Byte.class,    (byte)4,  (byte)5, (byte)6},
		   };
		   return Arrays.asList(data);
	   }
	

	private final Object valueCheck;
	private final Class<?> valueType;
	private final Object valueA;
	private final Object valueB;
	
	public ExecutableModelPrimativeConversionErrorsTests(Class<?> valueType, Class<?> valueCheckType, Object valueCheck, Object valueA, Object valueB) {
		this.valueCheck = valueCheck;
		this.valueType = valueType;
		this.valueA = valueA;
		this.valueB = valueB;
	}
	
	@Test
	public void withExecutableModel() throws IOException {
		
		KieBase kbase = loadRules(this.valueType, this.valueCheck, true);
		
		KieSession session = kbase.newKieSession();
		List<Object> values = new ArrayList<>();
		ClassWithValue ca = makeClassWithValue(this.valueA);
		ClassWithValue cb = makeClassWithValue(this.valueB);

		session.insert(values);
		session.insert(ca);
		session.insert(cb);
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
		
		assertEquals(2, values.size());
		assertTrue(values.contains(this.valueA));
		assertTrue(values.contains(this.valueB));
	}

	@Test
	public void withoutExecutableModel() throws IOException {
		
		KieBase kbase = loadRules(this.valueType, this.valueCheck, false);
		
		KieSession session = kbase.newKieSession();
		List<Object> values = new ArrayList<>();
		ClassWithValue ca = makeClassWithValue(this.valueA);
		ClassWithValue cb = makeClassWithValue(this.valueB);

		session.insert(values);
		session.insert(ca);
		session.insert(cb);
		
		session.startProcess(PROCESS_ID);
		
		session.fireAllRules();
		
		assertEquals(2, values.size());
		assertTrue(values.contains(this.valueA));
		assertTrue(values.contains(this.valueB));
	}
	
	private static KieBase loadRules(Class<?> valueType, Object value, boolean useExecutable) throws IOException {

		Path resources = Paths.get("src", "test", "resources");
		
		KieServices services = KieServices.Factory.get();
		KieFileSystem kfs = services.newKieFileSystem();

		String drlString = String.join(
				"\n",
				Files.readAllLines(resources.resolve("drl/rules.drl")))
				.replace("<value_type>", valueType.getSimpleName())
				.replace("<test_value>", getValueString(value));
		
		Resource drl = services.getResources().newByteArrayResource(drlString.getBytes());

		
		Resource rf = services.getResources()
				.newFileSystemResource(
						resources.resolve("rf/ruleflow.rf").toFile());

		kfs.write("src/main/resources/rules.drl", drl);
		kfs.write(rf);
		
		KieBuilder builder = services.newKieBuilder(kfs);
		
		builder = useExecutable ?
				builder.buildAll(ExecutableModelProject.class) :
					builder.buildAll();
		
		if (builder.getResults().hasMessages(Level.ERROR)) {
	        List<Message> errors = builder.getResults().getMessages(Level.ERROR);
	        StringBuilder sb = new StringBuilder("Errors:");
	        for (Message msg : errors) {
	            sb.append("\n  " + prettyBuildMessage(msg));
	        }
	        
	        throw new RuntimeException(sb.toString());
	    }
		
		KieContainer container = services.newKieContainer(
				services.getRepository().getDefaultReleaseId());
		
		return container.getKieBase();
	}
	
	private static String getValueString(Object value) {
		
		if(value instanceof Float) {
			return String.format("%.2ff", value);
			
		} else if(value instanceof Double) {
			return String.format("%.2f", value);
			
		} else {
			return value.toString();
			
		}
	}

	private static String prettyBuildMessage(Message msg) {
	    return "Message: {"
	        + "id="+ msg.getId()
	        + ", level=" + msg.getLevel()
	        + ", path=" + msg.getPath()
	        + ", line=" + msg.getLine()
	        + ", column=" + msg.getColumn()
	        + ", text=\"" + msg.getText() + "\""
	        + "}";
	}
	
	private static ClassWithValue makeClassWithValue(Object value) {
		
		ClassWithValue cwv = new ClassWithValue();

		if(value instanceof Float) {
			cwv.setValueFloat((float) value);
			
		} else if(value instanceof Double) {
			cwv.setValueDouble((double) value);
			
		} else if(value instanceof Integer) {
			cwv.setValueInteger((int) value);
			
		} else if(value instanceof Short) {
			cwv.setValueShort((short) value);
			
		} else if(value instanceof Byte) {
			cwv.setValueByte((byte) value);
			
		}
		
		return cwv;
	}
}

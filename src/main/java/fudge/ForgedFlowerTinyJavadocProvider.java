package fudge;

import fudge.forgedflower.api.IFabricJavadocProvider;
import fudge.forgedflower.struct.StructClass;
import fudge.forgedflower.struct.StructField;
import fudge.forgedflower.struct.StructMethod;
import net.fabricmc.mapping.tree.*;
import net.fabricmc.mappings.EntryTriple;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unfortunately IFabricJavadocProvider is too entangled in FF api, so we need to reimplement it
 */
public class ForgedFlowerTinyJavadocProvider implements IFabricJavadocProvider {
	private final Map<String, ClassDef> classes = new HashMap<>();
	private final Map<EntryTriple, FieldDef> fields = new HashMap<>();
	private final Map<EntryTriple, MethodDef> methods = new HashMap<>();

	private final String namespace = "named";

	public ForgedFlowerTinyJavadocProvider(File tinyFile) {
		final TinyTree mappings = readMappings(tinyFile);

		for (ClassDef classDef : mappings.getClasses()) {
			final String className = classDef.getName(namespace);
			classes.put(className, classDef);

			for (FieldDef fieldDef : classDef.getFields()) {
				fields.put(new EntryTriple(className, fieldDef.getName(namespace), fieldDef.getDescriptor(namespace)), fieldDef);
			}

			for (MethodDef methodDef : classDef.getMethods()) {
				methods.put(new EntryTriple(className, methodDef.getName(namespace), methodDef.getDescriptor(namespace)), methodDef);
			}
		}
	}

	@Override
	public String getClassDoc(StructClass structClass) {
		ClassDef classDef = classes.get(structClass.qualifiedName);
		return classDef != null ? classDef.getComment() : null;
	}

	@Override
	public String getFieldDoc(StructClass structClass, StructField structField) {
		FieldDef fieldDef = fields.get(new EntryTriple(structClass.qualifiedName, structField.getName(), structField.getDescriptor()));
		return fieldDef != null ? fieldDef.getComment() : null;
	}

	@Override
	public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
		MethodDef methodDef = methods.get(new EntryTriple(structClass.qualifiedName, structMethod.getName(), structMethod.getDescriptor()));

		if (methodDef != null) {
			List<String> parts = new ArrayList<>();

			if (methodDef.getComment() != null) {
				parts.add(methodDef.getComment());
			}

			boolean addedParam = false;

			for (ParameterDef param : methodDef.getParameters()) {
				String comment = param.getComment();

				if (comment != null) {
					if (!addedParam && methodDef.getComment() != null) {
						//Add a blank line before params when the method has a comment
						parts.add("");
						addedParam = true;
					}

					parts.add(String.format("@param %s %s", param.getName(namespace), comment));
				}
			}

			if (parts.isEmpty()) {
				return null;
			}

			return String.join("\n", parts);
		}

		return null;
	}

	private static TinyTree readMappings(File input) {
		try (BufferedReader reader = Files.newBufferedReader(input.toPath())) {
			return TinyMappingFactory.loadWithDetection(reader);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read mappings", e);
		}
	}
}

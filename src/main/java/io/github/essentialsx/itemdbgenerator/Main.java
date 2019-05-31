package io.github.essentialsx.itemdbgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.essentialsx.itemdbgenerator.providers.alias.AliasProvider;
import io.github.essentialsx.itemdbgenerator.providers.alias.SimpleAliasProvider;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import io.github.essentialsx.itemdbgenerator.providers.item.MaterialEnumProvider;
import io.github.essentialsx.itemdbgenerator.providers.item.SpawnerProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Path outputPath = Paths.get(".", "items.json");

    private static final List<ItemProvider> itemProviders = Arrays.asList(
        new MaterialEnumProvider(),
        new SpawnerProvider()
    );

    private static final List<AliasProvider> aliasProviders = Arrays.asList(
        new SimpleAliasProvider()
    );

    public static void main( String[] args ) {
        System.err.println("Generating items.json...");

        Set<ItemProvider.Item> items = getItems();
        JsonObject itemMap = new JsonObject();

        items.forEach(item -> {
            itemMap.add(item.getName(), gson.toJsonTree(item));
            getAliases(item).forEach(alias -> itemMap.addProperty(alias, item.getName()));
        });

        save(itemMap);

        System.err.println("Finished generating items.json");
    }

    private static void save(JsonObject itemMap) {
        String output = "#version: ${full.version}\n" + gson.toJson(itemMap);

        try {
            Files.deleteIfExists(outputPath);
            Files.write(outputPath, output.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save items.json!");
            e.printStackTrace();
        }

        System.out.println(output);
    }

    private static Set<ItemProvider.Item> getItems() {
        return itemProviders.parallelStream()
            .flatMap(ItemProvider::get)
            .collect(Collectors.toSet());
    }

    private static Set<String> getAliases(ItemProvider.Item item) {
        return aliasProviders.parallelStream()
            .flatMap(provider -> provider.get(item))
            .collect(Collectors.toSet());
    }
}
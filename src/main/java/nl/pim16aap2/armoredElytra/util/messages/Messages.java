package nl.pim16aap2.armoredElytra.util.messages;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Messages {
    private static final String DEFAULTFILENAME = "en_US.txt";
    private static final Pattern matchDots = Pattern.compile("\\.");
    private static final Pattern matchNewLines = Pattern.compile("\\\\n");
    private static final Pattern matchColorCodes = Pattern.compile("&((?i)[0-9a-fk-or])");
    private final ArmoredElytra plugin;
    /**
     * The map of all messages.
     * <p>
     * Key: The {@link Message} enum entry.
     * <p>
     * Value: The translated message.
     */
    private final Map<Message, String> messageMap = new EnumMap<>(Message.class);
    private File textFile;

    public Messages(final ArmoredElytra plugin) {
        this.plugin = plugin;
        writeDefaultFile();
        final String fileName = plugin.getConfigLoader().languageFile();
        // Only append .txt if the provided name doesn't already have it.
        textFile = new File(plugin.getDataFolder(), fileName.endsWith(".txt") ? fileName : (fileName + ".txt"));

        if (!textFile.exists()) {
            plugin.myLogger(Level.WARNING, "Failed to load language file: \"" + textFile +
                    "\": File not found! Using default file (\"" + DEFAULTFILENAME + "\") instead!");
            textFile = new File(plugin.getDataFolder(), DEFAULTFILENAME);
        }
        populateMessageMap();
    }

    private void writeDefaultFile() {
        File defaultFile = new File(plugin.getDataFolder(), DEFAULTFILENAME);

        InputStream in = null;
        try {
            URL url = getClass().getClassLoader().getResource(DEFAULTFILENAME);
            if (url == null)
                plugin.myLogger(Level.SEVERE, "Failed to read resources file from the jar! " +
                        "The default translation file cannot be generated! Please contact pim16aap2");
            else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                in = connection.getInputStream();
                java.nio.file.Files.copy(in, defaultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            plugin.myLogger(Level.SEVERE, "Failed to write default file to \"" + textFile + "\".");
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the contents of a file. Each valid line will be split up in the message key and the message value. It
     * then
     *
     * @param br
     *         The {@link BufferedReader} that supplies the text.
     * @param action
     *         The action to take for every message and value combination that is encountered.
     *
     * @throws IOException
     */
    private void processFile(final BufferedReader br, final BiConsumer<Message, String> action)
            throws IOException {
        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {
            // Ignore comments.
            if (sCurrentLine.startsWith("#") || sCurrentLine.isEmpty())
                continue;

            String[] parts = sCurrentLine.split("=", 2);
            try {
                final Message msg = Message.valueOf(matchDots.matcher(parts[0]).replaceAll("_").toUpperCase());
                final String value = matchNewLines.matcher(matchColorCodes.matcher(parts[1]).replaceAll("\u00A7$1"))
                        .replaceAll("\n");
                action.accept(msg, value);
            } catch (IllegalArgumentException e) {
                plugin.myLogger(Level.WARNING, "Failed to identify Message corresponding to key: \"" + parts[0] +
                        "\". Its value will be ignored!");

                System.out.println(
                        "Trying to find enum value of: " + matchDots.matcher(parts[0]).replaceAll("_").toUpperCase());
            }
        }
    }

    /**
     * Adds a message to the {@link #messageMap}.
     *
     * @param message
     *         The {@link Message}.
     * @param value
     *         The value of the message.
     */
    private void addMessage(final Message message, final String value) {
        messageMap.put(message, value);
    }

    /**
     * Adds a message to the {@link #messageMap} if it isn't on the map already.
     *
     * @param message
     *         The {@link Message}.
     * @param value
     *         The value of the message.
     */
    private void addBackupMessage(final Message message, final String value) {
        if (messageMap.containsKey(message))
            return;

        plugin.myLogger(Level.WARNING,
                "Could not find translation of key: \"" + message.name() + "\". Using default value instead!");
        addMessage(message, value);
    }

    /**
     * Reads the translations from the provided translations file.
     * <p>
     * Missing translations will use their default value.
     */
    private void populateMessageMap() {
        try (BufferedReader br = new BufferedReader(new FileReader(textFile))) {
            processFile(br, this::addMessage);
        } catch (FileNotFoundException e) {
            plugin.myLogger(Level.SEVERE, "Locale file \"" + textFile + "\" does not exist!");
            e.printStackTrace();
        } catch (IOException e) {
            plugin.myLogger(Level.SEVERE, "Could not read locale file! \"" + textFile + "\"");
            e.printStackTrace();
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULTFILENAME)).openStream()))) {
            processFile(br, this::addBackupMessage);
        } catch (FileNotFoundException e) {
            plugin.myLogger(Level.SEVERE, "Failed to load internal locale file!");
            e.printStackTrace();
        } catch (IOException e) {
            plugin.myLogger(Level.SEVERE, "Could not read internal locale file!");
            e.printStackTrace();
        }

        for (final Message msg : Message.values())
            if (!msg.equals(Message.EMPTY) && !messageMap.containsKey(msg)) {
                plugin.myLogger(Level.WARNING, "Could not find translation of key: " + msg.name());
                messageMap.put(msg, getFailureString(msg.name()));
            }
    }

    /**
     * Gets the default String to return in case a value could not be found for a given String.
     *
     * @param key
     *         The key that could not be resolved.
     *
     * @return The default String to return in case a value could not be found for a given String.
     */
    private String getFailureString(final String key) {
        return "Translation for key \"" + key + "\" not found! Contact server admin!";
    }

    /**
     * Gets the translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     *
     * @param msg
     *         The {@link Message} to translate.
     * @param values
     *         The values to substitute for the variables in the message.
     *
     * @return The translated message of the provided {@link Message} and substitutes its variables for the provided
     *         values.
     */
    public String getString(final Message msg, final String... values) {
        if (msg.equals(Message.EMPTY))
            return "";

        if (values.length != Message.getVariableCount(msg)) {
            plugin.myLogger(Level.SEVERE,
                    "Expected " + Message.getVariableCount(msg) + " variables for key " + msg.name() +
                            " but only got " + values.length + ". This is a bug. Please contact pim16aap2!");
            return getFailureString(msg.name());
        }

        String value = messageMap.get(msg);
        if (value != null) {
            for (int idx = 0; idx != values.length; ++idx)
                value = value.replaceAll(Message.getVariableName(msg, idx), values[idx]);
            return value;
        }

        plugin.myLogger(Level.WARNING, "Failed to get the translation for key " + msg.name());
        return getFailureString(msg.name());
    }
}

package org.iotacontrolcenter.properties.source;

import java.io.*;
import java.rmi.NotBoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.iotacontrolcenter.dto.IccrIotaNeighborsPropertyDto;
import org.iotacontrolcenter.dto.NeighborDto;

public class PropertySource {

    private static PropertySource instance;
    private static Object SYNC_INST = new Object();
    public static PropertySource getInstance() {
        synchronized (SYNC_INST) {
            if(PropertySource.instance == null) {
                PropertySource.instance = new PropertySource();
            }
            return PropertySource.instance;
        }
    }

    private static final Pattern PATTERN_TRUE = Pattern.compile("1|on|true|yes", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FALSE = Pattern.compile("0|off|false|no", Pattern.CASE_INSENSITIVE);
    private static final String CONF_FILE = "iccr.properties";

    public static final String ICCR_DIR_PROP = "iccrDir";
    public static final String ICCR_DIR_DEFAULT = "/opt/iccr";

    private static final String LOC_COUNTRY_PROP = "iccrCountryLocale";
    private static final String LOC_LANG_PROP = "iccrLanguageLocale";
    private static final String LOC_LANG_DEFAULT = "en";
    private static final String LOC_COUNTRY_DEFAULT = "US";

    public static final String ICCR_START_AT_START_PROP="iccrStartAtStartup";
    public static final String ICCR_START_IOTA_AT_START_PROP="iccrStartIotaAtStartup";
    public static final String ICCR_STOP_IOTA_AT_SHUTDOWN_PROP="iccrStopIotaAtShutdown";
    public static final String ICCR_PORT_NUMBER_PROP = "iccrPortNumber";

    public static final String IOTA_DLD_LINK_PROP="iotaDownloadLink";
    public static final String IOTA_DLD_FILENAME_PROP="iotaDownloadFilename";

    public static final String IOTA_APP_DIR_PROP = "iotaDir";
    public static final String IOTA_START_PROP="iotaStartCmd";
    public static final String IOTA_PORT_NUMBER_PROP="iotaPortNumber";

    public static final String IOTA_NBR_REFRESH_PROP="iotaNeighborRefreshTime";
    public static final String IOTA_NEIGHBORS_PROP="iotaNeighbors";
    public static final String IOTA_NEIGHBOR_PROP_PREFIX="iotaNeighbor";

    private Properties props;
    private String bakDir;
    private String binDir;
    private String confDir;
    private String confFile;
    private String dataDir;
    private String dldDir;
    private String iccrDir;
    private String osName;
    private String tmpDir;
    private PropertiesConfiguration propWriter;
    private DateTimeFormatter ymdhmsFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PropertySource() {
        System.out.println("new PropertySource");

        osName = System.getProperty("os.name").toLowerCase();

        System.out.println("os.name: " + osName);

        iccrDir = System.getProperty(ICCR_DIR_PROP);
        if(iccrDir == null || iccrDir.isEmpty()) {
            System.out.println(ICCR_DIR_PROP + " system setting not available, using default: " + ICCR_DIR_DEFAULT);
            iccrDir = ICCR_DIR_DEFAULT;
        }

        bakDir = iccrDir + "/bak";
        binDir = iccrDir + "/bin";
        confDir = iccrDir + "/conf";
        dldDir = iccrDir + "/download";
        dataDir = iccrDir + "/data";
        tmpDir = iccrDir + "/tmp";


        confFile = confDir + "/" + CONF_FILE;

        try {
            propWriter = new PropertiesConfiguration(confFile);
        }
        catch(Exception e) {
            System.out.println("PropertySource exception creating PropertiesConfiguration: " + e.getLocalizedMessage());
        }

        props = new Properties();
        load();
    }

    public void load() {
        try {
            InputStream is = new FileInputStream(confFile);
            props.load(is);
        }
        catch(Exception e) {
            System.out.println("failed to load iccr.properties from " + confDir);
            e.printStackTrace();
        }
    }

    public String getNowDateTimestamp() {
        return ymdhmsFormatter.format(LocalDateTime.now());
    }

    public String getIotaDownloadUrl() {
        return getString(IOTA_DLD_LINK_PROP) + "/" + getIotaDownloadFilename();
    }

    public String getIotaDownloadFilename() {
        return getString(IOTA_DLD_FILENAME_PROP);
    }

    public String getIotaStartCmd() {
        return getString(IOTA_START_PROP);
    }

    public String getIotaAppDir() {
        return getString(IOTA_APP_DIR_PROP);
    }

    public String getIriJarFilePath() {
        return getIotaAppDir() + "/" + getIriJarFileInStartCmd();
    }

    public String getIriJarFileInStartCmd() {
        // Something like: java -jar IRI.jar
        String iotaStartCmd = getIotaStartCmd();
        String jarFile = iotaStartCmd.replaceAll("^.*java.*-jar +", "");
        jarFile = jarFile.replaceAll("\\.jar.*$",".jar");
        return jarFile;
    }

    public String getLocalIotaUrl() {
        return "http://localhost:" + getString(IOTA_PORT_NUMBER_PROP) + "/";
    }

    public String getIccrBinDir() {
        return binDir;
    }

    public String getIccrConfDir() {
        return confDir;
    }

    public String getIccrBakDir() {
        return bakDir;
    }

    public String getIccrDownloadDir() {
        return dldDir;
    }

    public String getIccrDataDir() {
        return dataDir;
    }

    public String getIccrTmpDir() {
        return tmpDir;
    }

    public String getIccrDir() {
        return iccrDir;
    }

    public String getOsName() {
        return osName;
    }

    public boolean osIsWindows() {
        return (osName.indexOf("win") >= 0);
    }

    public boolean osIsMax() {
        return (osName.indexOf("mac") >= 0);
    }

    public void setProperty(String key, Object value) {

            props.setProperty(key, (String)value);
            propWriter.setProperty(key, value);

            try {
                propWriter.save();
            }
            catch(Exception e) {
                System.out.println("PropertySource exception saving PropertiesConfiguration: " + e.getLocalizedMessage());
            }
    }

    public List<String> getPropertyKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(ICCR_START_AT_START_PROP);
        keys.add(ICCR_START_IOTA_AT_START_PROP);
        keys.add(ICCR_STOP_IOTA_AT_SHUTDOWN_PROP);
        keys.add(ICCR_PORT_NUMBER_PROP);
        keys.add(IOTA_PORT_NUMBER_PROP);
        keys.add(IOTA_DLD_LINK_PROP);
        keys.add(IOTA_DLD_FILENAME_PROP);
        keys.add(IOTA_APP_DIR_PROP);
        keys.add(IOTA_START_PROP);
        keys.add(IOTA_NBR_REFRESH_PROP);
        return keys;
    }

    public String getLocaleLanguage() {
        String val = getString(LOC_LANG_PROP);
        if(val == null || val.isEmpty()) {
            val = LOC_LANG_DEFAULT;
        }
        return val;
    }

    public String getLocaleCountry() {
        String val = getString(LOC_COUNTRY_PROP);
        if(val == null || val.isEmpty()) {
            val = LOC_COUNTRY_DEFAULT;
        }
        return val;
    }

    public List<String> getNeighborKeys() {
        return getList(IOTA_NEIGHBORS_PROP);
    }

    public String getString(String key) {
        return props.getProperty(key);
    }

    public boolean getBoolean(String key) {
        String val = props.getProperty(key);
        if(val != null) {
            if(PATTERN_TRUE.matcher(val).matches()) {
                return true;
            }
            else if(PATTERN_FALSE.matcher(val).matches()) {
                return false;
            }
            else {
                throw new IllegalArgumentException("Invalid boolean value provided for " + key);
            }
        }
        throw new IllegalArgumentException("No value provided for " + key);
    }

    public int getInteger(String key) {
        String val = props.getProperty(key);
        if(val != null) {
            try {
                return Integer.parseInt(val);
            }
            catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid integer value provided for " + key);
            }
        }
        throw new IllegalArgumentException("No value provided for " + key);
    }

    public List<String> getList(String key) {
        List<String> keys = new ArrayList<>();
        String val = getString(key);
        if(val != null && !val.isEmpty()) {
            if(val.contains(",")) {
                String[] arr = val.split(",");
                for(String id : arr) {
                    keys.add(id.trim());
                }
            }
            else {
                keys.add(val);
            }
        }
        return keys;
    }

    public void setIotaNeighbors(IccrIotaNeighborsPropertyDto nbrs) {
        String nbrKeys = nbrs.nbrKeys();
        String id;
        for(NeighborDto nbr : nbrs.getNbrs()) {
            System.out.println("updated neighbor: " + nbr);
            id = nbr.getKey();
            setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".key." + id, nbr.getKey());
            setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".ip." + id, nbr.getIp());
            if(nbr.getName() != null) {
                setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".name." + id, nbr.getName());
            }
            if(nbr.getDescr() != null) {
                setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".descr." + id, nbr.getDescr());
            }
            setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".active." + id, String.valueOf(nbr.isActive()).toLowerCase());
            setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".port." + id, Integer.toString(nbr.getPort()));
            setProperty(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".scheme." + id, nbr.getScheme());
        }
        setProperty(IOTA_NEIGHBORS_PROP, nbrKeys);
    }

    public IccrIotaNeighborsPropertyDto getIotaNeighbors() {
        List<NeighborDto> nbrs = new ArrayList<>();
        for(String id : getNeighborKeys()) {
            try {
                nbrs.add(new NeighborDto(
                        getString(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".key." + id),
                        getString(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".ip." + id),
                        getString(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".name." + id),
                        getString(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".descr." + id),
                        getBoolean(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".active." + id),
                        getInteger(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".port." + id),
                        getString(PropertySource.IOTA_NEIGHBOR_PROP_PREFIX + ".scheme." + id)));
            }
            catch(Exception e) {
                System.out.println("getIotaNeighborsProperty exception: " + e.getLocalizedMessage());
            }
        }
        return new IccrIotaNeighborsPropertyDto(PropertySource.IOTA_NEIGHBORS_PROP, nbrs);
    }

}
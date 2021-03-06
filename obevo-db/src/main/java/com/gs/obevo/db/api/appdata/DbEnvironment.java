/**
 * Copyright 2017 Goldman Sachs.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gs.obevo.db.api.appdata;

import java.sql.Driver;

import com.gs.obevo.api.appdata.Environment;
import com.gs.obevo.api.appdata.PhysicalSchema;
import com.gs.obevo.api.platform.Platform;
import com.gs.obevo.db.api.factory.DbPlatformConfiguration;
import com.gs.obevo.db.api.platform.DbDeployerAppContext;
import com.gs.obevo.db.api.platform.DbPlatform;
import com.gs.obevo.db.api.platform.DbTranslationDialect;
import com.gs.obevo.util.inputreader.Credential;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.factory.Lists;

public class DbEnvironment extends Environment<DbPlatform> {
    private String dbHost;
    private int dbPort;
    private String dbServer;
    private char dataDelimiter = ',';
    private String nullToken = "null";
    private String tokenPrefix = "${";
    private String tokenSuffix = "}";
    private DbPlatform systemDbPlatform;
    private boolean autoReorgEnabled;
    private boolean persistToFile;
    private boolean disableAuditTracking;
    private ImmutableList<Permission> permissions = Lists.immutable.empty();

    private String jdbcUrl;
    private String dbDataSourceName;
    private String driverClassName;
    private String defaultTablespace;

    private ImmutableList<Group> groups = Lists.immutable.empty();
    private ImmutableList<User> users = Lists.immutable.empty();

    private String auditTableSql;
    private ImmutableSet<String> acceptedExtensions;
    private String ianywhereDriverProperty;
    private boolean checksumDetectionEnabled = false;
    private boolean invalidObjectCheckEnabled = true;
    private boolean reorgCheckEnabled = true;
    private int metadataLineReaderVersion = DbPlatformConfiguration.getInstance().getFeatureToggleVersion("metadataLineReaderVersion");
    private int csvVersion = DbPlatformConfiguration.getInstance().getFeatureToggleVersion("csvVersion");

    public DbEnvironment() {
    }

    public DbEnvironment createCopy() {
        DbEnvironment env = new DbEnvironment();
        env.copyFieldsFrom(this);
        return env;
    }

    @Override
    public void copyFieldsFrom(Environment baseEnv) {
        DbEnvironment env = (DbEnvironment) baseEnv;
        super.copyFieldsFrom(env);
        this.dbHost = env.dbHost;
        this.dbPort = env.dbPort;
        this.dbServer = env.dbServer;
        this.dataDelimiter = env.dataDelimiter;
        this.nullToken = env.nullToken;
        this.tokenPrefix = env.tokenPrefix;
        this.tokenSuffix = env.tokenSuffix;
        this.systemDbPlatform = env.systemDbPlatform;
        this.autoReorgEnabled = env.autoReorgEnabled;
        this.persistToFile = env.persistToFile;
        this.disableAuditTracking = env.disableAuditTracking;
        this.permissions = env.permissions;
        this.jdbcUrl = env.jdbcUrl;
        this.dbDataSourceName = env.dbDataSourceName;
        this.driverClassName = env.driverClassName;
        this.defaultTablespace = env.defaultTablespace;
        this.groups = env.groups;
        this.users = env.users;
        this.auditTableSql = env.auditTableSql;
        this.acceptedExtensions = env.acceptedExtensions;
        this.ianywhereDriverProperty = env.ianywhereDriverProperty;
        this.checksumDetectionEnabled = env.checksumDetectionEnabled;
        this.invalidObjectCheckEnabled = env.invalidObjectCheckEnabled;
        this.reorgCheckEnabled = env.reorgCheckEnabled;
        this.metadataLineReaderVersion = env.metadataLineReaderVersion;
        this.csvVersion = env.csvVersion;
    }

    @Override
    public String getDisplayString() {
        String connectionInfo;
        if (this.getJdbcUrl() != null) {
            connectionInfo = this.getJdbcUrl();
        } else if (this.getDbDataSourceName() != null) {
            connectionInfo = this.getDbDataSourceName();
        } else if (this.getDbServer() != null) {
            connectionInfo = this.getDbHost() + ":" + this.getDbPort() + "/" + this.getDbServer();
        } else {
            connectionInfo = this.getDbHost() + ":" + this.getDbPort();
        }

        return super.getDisplayString() + ": Connecting to [" + connectionInfo + "] against schemas ["
                + this.getPhysicalSchemas().collect(PhysicalSchema.TO_PHYSICAL_NAME).makeString(",") + "]";
    }

    public String getDbHost() {
        return this.dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return this.dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbServer() {
        return this.dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbDataSourceName() {
        return this.dbDataSourceName;
    }

    public void setDbDataSourceName(String dbDataSourceName) {
        this.dbDataSourceName = dbDataSourceName;
    }

    public String getTokenPrefix() {
        return this.tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix == null ? "" : tokenPrefix;
    }

    public String getTokenSuffix() {
        return this.tokenSuffix;
    }

    public void setTokenSuffix(String tokenSuffix) {
        this.tokenSuffix = tokenSuffix == null ? "" : tokenSuffix;
    }

    /**
     * @deprecated use {@link #getPlatform()}
     */
    @Deprecated
    public DbPlatform getDbPlatform() {
        return this.getPlatform();
    }

    /**
     * @deprecated use {@link #setPlatform(Platform)}
     */
    @Deprecated
    public void setDbPlatform(DbPlatform dbPlatform) {
        this.setPlatform(dbPlatform);
    }

    @Override
    public void setPlatform(DbPlatform platform) {
        super.setPlatform(platform);
        this.setAppContextBuilderClass(platform.getAppContextBuilderClass());
    }

    public DbPlatform getSystemDbPlatform() {
        return this.systemDbPlatform != null ? this.systemDbPlatform : this.getPlatform();
    }

    public void setSystemDbPlatform(DbPlatform systemDbPlatform) {
        this.systemDbPlatform = systemDbPlatform;
    }

    public boolean isAutoReorgEnabled() {
        return this.autoReorgEnabled;
    }

    public void setAutoReorgEnabled(boolean autoReorgEnabled) {
        this.autoReorgEnabled = autoReorgEnabled;
    }

    public ImmutableList<Permission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(ImmutableList<Permission> permissions) {
        this.permissions = permissions;
    }

    public char getDataDelimiter() {
        return this.dataDelimiter;
    }

    public void setDataDelimiter(char dataDelimiter) {
        this.dataDelimiter = dataDelimiter;
    }

    public String getNullToken() {
        return this.nullToken;
    }

    public void setNullToken(String nullToken) {
        this.nullToken = nullToken;
    }

    public boolean isPersistToFile() {
        return this.persistToFile;
    }

    public void setPersistToFile(boolean persistToFile) {
        this.persistToFile = persistToFile;
    }

    public boolean isDisableAuditTracking() {
        return this.disableAuditTracking;
    }

    public void setDisableAuditTracking(boolean disableAuditTracking) {
        this.disableAuditTracking = disableAuditTracking;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Class<? extends Driver> getDriverClass() {
        if (this.driverClassName != null) {
            try {
                return (Class<? extends Driver>) Class.forName(this.driverClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return this.getPlatform().getDriverClass(this);
        }
    }

    /**
     * Sets the default driver class to use.
     *
     * @see #setDriverClassName(String)
     * @deprecated Use {@link #setDriverClassName(String)} instead. Will retire in next version.
     */
    @Deprecated
    public void setDriverClass(Class<? extends Driver> driverClass) {
        this.setDriverClassName(driverClass.getName());
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDefaultTablespace() {
        return this.defaultTablespace;
    }

    public void setDefaultTablespace(String defaultTablespace) {
        this.defaultTablespace = defaultTablespace;
    }

    public DbTranslationDialect getDbTranslationDialect() {
        return getSystemDbPlatform().getDbTranslationDialect(this.getPlatform());
    }

    @Override
    public DbDeployerAppContext getAppContextBuilder() {
        return (DbDeployerAppContext) super.getAppContextBuilder();
    }

    public DbDeployerAppContext buildAppContext() {
        return this.getAppContextBuilder().build();
    }

    public DbDeployerAppContext buildAppContext(Credential credential) {
        return this.getAppContextBuilder().setCredential(credential).build();
    }

    public DbDeployerAppContext buildAppContext(String username, String password) {
        return this.getAppContextBuilder().setCredential(new Credential(username, password)).build();
    }

    public ImmutableList<Group> getGroups() {
        // ideally, the groups are defined in the groups field; however, we have not enforced this in the past, and most
        // folks just define the groups via the permission schemes. Hence, we add this extra getter here to allow
        // clients to get the groups they need
        MutableSet<String> groupNames = permissions.flatCollect(Permission.TO_GRANTS).flatCollect(new Function<Grant, Iterable<String>>() {
            @Override
            public Iterable<String> valueOf(Grant grant) {
                return grant.getGrantTargets().get(GrantTargetType.GROUP);
            }
        }).toSet();

        MutableSet<Group> permissionGroups = groupNames.collect(new Function<String, Group>() {
            @Override
            public Group valueOf(String groupName) {
                return new Group(groupName);
            }
        });

        permissionGroups.removeIf(Predicates.attributeIn(Group.TO_NAME, this.groups.collect(Group.TO_NAME)));  // ensure that we don't duplicate groups across the permissions and config groups list

        return this.groups.newWithAll(permissionGroups);
    }

    public void setGroups(ImmutableList<Group> groups) {
        this.groups = groups;
    }

    public ImmutableList<User> getUsers() {
        // See note in getGroups() on why we get the users from the permissions
        MutableSet<String> userNames = permissions.flatCollect(Permission.TO_GRANTS).flatCollect(new Function<Grant, Iterable<String>>() {
            @Override
            public Iterable<String> valueOf(Grant grant) {
                return grant.getGrantTargets().get(GrantTargetType.USER);
            }
        }).toSet();  // remove duplicates within the permissions list

        MutableSet<User> permissionUsers = userNames.collect(new Function<String, User>() {
            @Override
            public User valueOf(String username) {
                return new User(username, null, false);
            }
        });

        permissionUsers.removeIf(Predicates.attributeIn(User.TO_NAME, this.users.collect(User.TO_NAME)));  // ensure that we don't duplicate users across the permissions and config users list

        return this.users.newWithAll(permissionUsers);
    }

    public void setUsers(ImmutableList<User> users) {
        this.users = users;
    }

    /**
     * property to store the auditTableSql, in case we want to override the default value as created in SameSchemaChangeAuditDao.
     * Ideally, this should never be set or used, but we have this "just in case" so that clients can work around issues
     * via this tool and not manually. This is notable for the new Deployer logic that detects if an INIT is required, as
     * that logic works in part if the audit table doesn't exist.
     */
    public String getAuditTableSql() {
        return auditTableSql;
    }

    /**
     * @see #getAuditTableSql()
     */
    public void setAuditTableSql(String auditTableSql) {
        this.auditTableSql = auditTableSql;
    }

    /**
     * Override the accepted extensions from the platform if needed.
     * This should be avoided if possible; clients should send feedback to the product maintainers to update the list of
     * defaults if needed (or to contribute the code change themselves).
     */
    public void setAcceptedExtensions(ImmutableSet<String> acceptedExtensions) {
        this.acceptedExtensions = acceptedExtensions;
    }

    public ImmutableSet<String> getAcceptedExtensions() {
        if (acceptedExtensions != null && acceptedExtensions.notEmpty()) {
            return acceptedExtensions;
        }
        return getPlatform().getAcceptedExtensions();
    }

    public String getIanywhereDriverProperty() {
        return ianywhereDriverProperty;
    }

    public void setIanywhereDriverProperty(String ianywhereDriverProperty) {
        this.ianywhereDriverProperty = ianywhereDriverProperty;
    }

    public boolean isChecksumDetectionEnabled() {
        return checksumDetectionEnabled;
    }

    public void setChecksumDetectionEnabled(boolean checksumDetectionEnabled) {
        this.checksumDetectionEnabled = checksumDetectionEnabled;
    }

    public boolean isInvalidObjectCheckEnabled() {
        return invalidObjectCheckEnabled;
    }

    public void setInvalidObjectCheckEnabled(boolean invalidObjectCheckEnabled) {
        this.invalidObjectCheckEnabled = invalidObjectCheckEnabled;
    }

    public boolean isReorgCheckEnabled() {
        return reorgCheckEnabled;
    }

    public void setReorgCheckEnabled(boolean reorgCheckEnabled) {
        this.reorgCheckEnabled = reorgCheckEnabled;
    }

    public int getMetadataLineReaderVersion() {
        return metadataLineReaderVersion;
    }

    public void setMetadataLineReaderVersion(int metadataLineReaderVersion) {
        this.metadataLineReaderVersion = metadataLineReaderVersion;
    }

    public int getCsvVersion() {
        return csvVersion;
    }

    public void setCsvVersion(int csvVersion) {
        this.csvVersion = csvVersion;
    }
}

#!/usr/bin/perl

# Script to publish mondrian schema
# Author: Kleyson Rios

use DBI;
use strict;
use MIME::Base64;
use JSON qw( decode_json );
use Getopt::Long qw(GetOptions);

my $user;
my $pentaho_server;
my $pentaho_port;
my $solution;
my $path;
my $mondrianSchema;
my $datasourceName;
my $xmlaFlag;

options();

######################################
#
# Dados para a conexao com o Banco de Dados
#
######################################
my $driver   = "Pg";
my $host     = "localhost";
my $port     = 5432;
my $database = "postgres";
my $dsn      = "DBI:$driver:dbname=$database;host=$host;port=$port";
my $userid   = "postgres";
my $password = "postgres";
my $dbh      = DBI->connect($dsn, $userid, $password, { RaiseError => 1 }) or die $DBI::errstr;

print "\nDatabase opened successfully\n";

######################################
#
# New token based on epoch time
#
######################################
my $token = time;

######################################
#
# Encode url
#
######################################
my $urlEncoded = encode_base64("/pentaho/plugin/fastsync/api/publish?ts=" . $token);
$urlEncoded =~ s/[\r\n]+$//;

######################################
#
# Insert token
#
######################################
my $stmt = qq( INSERT INTO public.tokens_bi(username, token, url) VALUES ('$user', '$token', '$urlEncoded'); );
my $rv = $dbh->do($stmt) or die $DBI::errstr;

print "Token inserted successfully\n";

######################################
#
# Call /sync API
#
######################################
my $api = "http://$pentaho_server:$pentaho_port/pentaho/plugin/fastsync/api/publish --data-urlencode 'type=token' --data-urlencode 'token=$token' --data-urlencode 'solution=$solution' --data-urlencode 'path=$path' --data-urlencode 'schema=$mondrianSchema' --data-urlencode 'datasourceName=$datasourceName' --data-urlencode 'xmlaEnabledFlag=$xmlaFlag' --data-urlencode 'urlEncoded=$urlEncoded'";

my $apiReturn = `/usr/bin/curl -s -G $api`;

my $decodedReturn = decode_json($apiReturn);

print $decodedReturn->{'error_message'} . "\n";
print $decodedReturn->{'message'} . "\n";


sub options() {

    GetOptions('user=s' => \$user,
               'host=s' => \$pentaho_server,
               'port=s' => \$pentaho_port,
               'solution=s' => \$solution,
               'path=s' => \$path,
               'schema=s' => \$mondrianSchema,
               'datasource=s' => \$datasourceName,
               'xmlaFlag=s' => \$xmlaFlag) or die "Usage: $0 --user PENTAHO_USER --host PENTAHO_SERVER --port PORT --solution SOLUTION --path PATH --schema MONDRIAN_SCHEMA --datasource JNDI_NAME --xmlaFlag TRUE/FALSE\n";

    if (! $solution || ! $path || ! $user || ! $pentaho_server || ! $pentaho_port || ! $mondrianSchema || ! $datasourceName || ! $xmlaFlag) {
         die "Usage: $0 --user PENTAHO_USER --host PENTAHO_SERVER --port PORT --solution SOLUTION --path PATH --schema MONDRIAN_SCHEMA --datasource JNDI_NAME --xmlaFlag TRUE/FALSE\n";
    }

}
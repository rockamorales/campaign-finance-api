CREATE KEYSPACE IF NOT EXISTS campaign_finance
WITH REPLICATION = { 'class' : 'SimpleStrategy','replication_factor':1 };


CREATE TABLE IF NOT EXISTS campaign_finance.candidate_contributions (
                                                           candidate_id int,
                                                           cycle int,
                                                           candidate_total double,
                                                           candidate_name text,
                                                           candidate_party text,
                                                           candidate_office text,
                                                           PRIMARY KEY (candidate_id, cycle));


CREATE TABLE IF NOT EXISTS campaign_finance.industry_contributions (
                                                                        industry_code int,
                                                                        candidate_id int,
                                                                        cycle int,
                                                                        industry_total double,
                                                                        industry_name text,
                                                                        PRIMARY KEY (industry_code, candidate_id, cycle));


CREATE TABLE IF NOT EXISTS campaign_finance.individuals_contributions (
                                                                       individual_id int,
                                                                       candidate_id int,
                                                                       cycle int,
                                                                       individual_total double,
                                                                       individual_name text,
                                                                       individual_employer text,
                                                                       candidate_name text,
                                                                       candidate_party text,
                                                                       employer_industry_code text,
                                                                       candidate_office text,
                                                                       PRIMARY KEY (individual_id, candidate_id, cycle));


CREATE TABLE IF NOT EXISTS campaign_finance.pacs_contributions (
                                                                  pac_id int,
                                                                  candidate_id int,
                                                                  cycle int,
                                                                  pac_total double,
                                                                  pac_name text,
                                                                  PRIMARY KEY (pac_id, candidate_id, cycle));

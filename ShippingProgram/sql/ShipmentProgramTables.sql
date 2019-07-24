create table if not exists Customer(
	custName varchar(50) NOT NULL,
	abbreviation varchar(6) NOT NULL,
	PPA char(1) NOT NULL,
	primary key (custName),
	check (PPA = 'Y' OR PPA = 'N')
);

create table if not exists Site(
	custName varchar(50) NOT NULL,
	siteName varchar(50) NOT NULL,
	streetAddress varchar(100) NOT NULL,
	stateAddress varchar(3),
	cityAddress varchar(15) NOT NULL,
	countryAddress varchar(25) NOT NULL,
	zipAddress varchar(9),
	primary key (custName, siteName),
	foreign key (custName) references Customer(custName) on delete cascade
);

create table if not exists AccountNumber(
	custName varchar(50) NOT NULL,
	siteName varchar(50) NOT NULL,
	serviceName varchar(5) NOT NULL,
	accountNum varchar(18) NOT NULL,
	billingZip varchar(5) NOT NULL,
	primary key (custName, siteName, serviceName),
	foreign key (custName, siteName) references Site(custName, siteName) on delete cascade
);
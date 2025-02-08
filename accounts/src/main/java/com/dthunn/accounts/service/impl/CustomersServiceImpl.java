package com.dthunn.accounts.service.impl;

import com.dthunn.accounts.dto.AccountsDto;
import com.dthunn.accounts.dto.CardsDto;
import com.dthunn.accounts.dto.CustomerDetailsDto;
import com.dthunn.accounts.dto.LoansDto;
import com.dthunn.accounts.entity.Accounts;
import com.dthunn.accounts.entity.Customer;
import com.dthunn.accounts.exception.ResourceNotFoundException;
import com.dthunn.accounts.mapper.AccountsMapper;
import com.dthunn.accounts.mapper.CustomerMapper;
import com.dthunn.accounts.repository.AccountsRepository;
import com.dthunn.accounts.repository.CustomerRepository;
import com.dthunn.accounts.service.ICustomersService;
import com.dthunn.accounts.service.client.CardsFeignClient;
import com.dthunn.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}

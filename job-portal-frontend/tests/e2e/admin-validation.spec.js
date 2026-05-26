import { expect, test } from '@playwright/test';
import { createJob, loginAsAdmin } from './helpers';

test('admin sees backend validation message when description exceeds the limit', async ({ page }) => {
    const uniqueId = Date.now();
    const title = `e2e invalid job ${uniqueId}`;
    const company = 'ACME Validation';
    const description = 'x'.repeat(2001);

    await loginAsAdmin(page);
    await expect(page.getByRole('heading', { name: 'Admin Dashboard' })).toBeVisible();

    await createJob(page, { title, company, description });

    await expect(page.getByText('Description must be at most 2000 characters')).toBeVisible();
    await expect(page.getByText('Job created successfully.')).toHaveCount(0);
    await expect(page.locator('.job-card').filter({ hasText: title })).toHaveCount(0);
});
